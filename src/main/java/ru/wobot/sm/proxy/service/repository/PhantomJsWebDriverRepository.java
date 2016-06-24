package ru.wobot.sm.proxy.service.repository;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.wobot.sm.proxy.driver.ProxiedWebDriver;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class PhantomJsWebDriverRepository implements WebDriverRepository {
    private static final Logger logger = LoggerFactory
            .getLogger(PhantomJsWebDriverRepository.class);

    private final ProxyRepository proxyRepository;
    private static final Collection<WebDriver> drivers = new ArrayList<>();

    @Autowired
    public PhantomJsWebDriverRepository(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
    }

    @PreDestroy
    public void cleanUp() {
        System.out.println("Spring Container is destroy! Customer clean up");
    }

    @Override
    public ProxiedWebDriver getDriver() {
        logger.info("Thread: " + Thread.currentThread().getId() + "; Trying to create browser...");

        String proxy = proxyRepository.getHost();
        logger.info("Proxy used: " + proxy);

        List<String> cliArgsCap = new ArrayList<>();
        cliArgsCap.add("--web-security=no");
        cliArgsCap.add("--ignore-ssl-errors=yes");
        cliArgsCap.add("--ssl-protocol=any");
        cliArgsCap.add("--proxy=" + proxy);
        cliArgsCap.add("--proxy-auth=" + proxyRepository.getCredentials(proxy));
        cliArgsCap.add("--proxy-type=http");
        cliArgsCap.add("--load-images=false");
        cliArgsCap.add("--webdriver-loglevel=ERROR");

        DesiredCapabilities caps = DesiredCapabilities.phantomjs();
        caps.setJavascriptEnabled(true);
        caps.setCapability("phantomjs.page.customHeaders." + "Accept-Language", "ru-RU");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);

        logger.info("Thread: " + Thread.currentThread().getId() + "; Browser created");
        WebDriver created = new PhantomJSDriver(caps);
        drivers.add(created);

        return new PhantomJsProxiedWebDriver(proxy, created);
    }

    public static class PhantomJsProxiedWebDriver implements ProxiedWebDriver {
        private final String proxy;
        private final WebDriver driver;

        public PhantomJsProxiedWebDriver(String proxy, WebDriver driver) {
            this.proxy = proxy;
            this.driver = driver;
        }

        @Override
        public String getProxy() {
            return proxy;
        }

        @Override
        public WebDriver getWebDriver() {
            return driver;
        }
    }

}
