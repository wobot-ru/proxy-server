package ru.wobot.sm.proxy.service.fetch;

import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.wobot.sm.proxy.driver.ProxiedWebDriver;
import ru.wobot.sm.proxy.service.repository.AccountRepository;
import ru.wobot.sm.proxy.service.repository.WebDriverRepository;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component
public class FbFetcher implements Fetcher {
    private static final Logger logger = LoggerFactory
            .getLogger(FbFetcher.class);

    private static final int WAIT_FOR_LOAD_TIMEOUT = 4;
    private static final String FACEBOOK_URI = "https://www.facebook.com";

    private final AccountRepository cookieRepository;
    private final ThreadLocal<ProxiedWebDriver> threadWebDriver;

    @Autowired
    public FbFetcher(AccountRepository cookieRepository, WebDriverRepository webDriverRepository) {
        this.cookieRepository = cookieRepository;

        this.threadWebDriver = new ThreadLocal<ProxiedWebDriver>() {
            @Override
            protected ProxiedWebDriver initialValue() {
                ProxiedWebDriver proxiedWebDriver = webDriverRepository.getDriver();
                proxiedWebDriver.getWebDriver().get(FACEBOOK_URI);
                return proxiedWebDriver;
            }
        };
    }

    @Override
    public String get(String url) {
        return get(url, 0, 0);
    }

    @Override
    public String get(String url, int numberOfLoadPages, int maxPageHeight) {
        ProxiedWebDriver proxiedWebDriver = threadWebDriver.get();
        WebDriver driver = proxiedWebDriver.getWebDriver();
        driver.manage().deleteAllCookies();

        Collection<Cookie> cookies = getCookies(proxiedWebDriver.getProxy());
        if (cookies.isEmpty())
            throw new IllegalStateException("No cookies found. Can't authorize web driver.");

        for (Cookie cookie : cookies)
            driver.manage().addCookie(cookie);

        driver.get(url);
        String currentUrl = driver.getCurrentUrl();
        logger.info("Thread: " + Thread.currentThread().getId() + "; Fetching URL: " + currentUrl + "; Original URL: " + url);

        driver.manage().timeouts().implicitlyWait(WAIT_FOR_LOAD_TIMEOUT, TimeUnit.SECONDS);
        try {
            driver.findElement(By.cssSelector("div._5vf._2pie._2pip.sectionHeader")); // wait for this facebook only element
        } catch (NoSuchElementException e) {
            logger.error("Thread: " + Thread.currentThread().getId() + "; No desired element for URL: " + currentUrl + "; Original URL: " + url);
        }

        JavascriptExecutor executor = (JavascriptExecutor) driver;
        try {
            int pageNumber = 0;
            while (true) {
                Object o = executor.executeScript("return document.body.scrollHeight;");
                Long height = (Long) o;
                executor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                driver.manage().timeouts().implicitlyWait(WAIT_FOR_LOAD_TIMEOUT, TimeUnit.SECONDS);
                logger.info("pageNumber={} height={} url={}", pageNumber, height, url);
                try {
                    driver.findElement(By.id("endOfResultsFooter"));
                    logger.info("Thread: " + Thread.currentThread().getId() + "; Tag with id #endOfResultsFooter was found " + currentUrl + "; Original URL: " + url);
                    break;
                } catch (NoSuchElementException ex) {
                }

                if (height > maxPageHeight || pageNumber++ > numberOfLoadPages) {
                    logger.info("Thread: " + Thread.currentThread().getId() + "; Break loading by condition(maxPageHeight=" + maxPageHeight + ", pageNumber=" + pageNumber + ")  " + currentUrl + "; Original URL: " + url);
                    break;
                }
            }
        } catch (Throwable e) {
            logger.error("Thread: " + Thread.currentThread().getId() + "; No desired element for URL: " + currentUrl + "; Original URL: " + url);
        }

        return driver.findElement(By.tagName("html")).getAttribute("innerHTML");
    }

    private Collection<Cookie> getCookies(String proxy) {
        Collection<Cookie> result = new ArrayList<>();
        for (HttpCookie cookie : cookieRepository.getCookies(proxy)) {
            result.add(new Cookie.Builder(cookie.getName(), cookie.getValue()).domain(cookie.getDomain()).build());
            // for debug only
            if (cookie.getName().equals("c_user"))
                logger.info("Thread: " + Thread.currentThread().getId() + "; Cookie used of user ID: " + cookie.getValue());
        }
        return result;
    }

}
