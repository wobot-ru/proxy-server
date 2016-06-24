package ru.wobot.sm.proxy.service.fetch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.wobot.sm.proxy.driver.ProxiedWebDriver;
import ru.wobot.sm.proxy.service.repository.AccountRepository;
import ru.wobot.sm.proxy.service.repository.WebDriverRepository;

import java.net.HttpCookie;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class FbFetcherTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private WebDriverRepository webDriverRepository;

    private FbFetcher fbFetcher;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ProxiedWebDriver proxiedWebDriver = mock(ProxiedWebDriver.class);
        WebDriver webDriver = mock(WebDriver.class, Mockito.RETURNS_MOCKS);
        WebElement mockElement = mock(WebElement.class);

        given(webDriverRepository.getDriver()).willReturn(proxiedWebDriver);
        given(proxiedWebDriver.getProxy()).willReturn("184.75.209.130:6060");
        given(proxiedWebDriver.getWebDriver()).willReturn(webDriver);
        given(webDriver.findElement(any(By.class))).willReturn(mockElement);
        given(mockElement.getAttribute("innerHTML")).willReturn("Valid html");
        given(accountRepository.getCookies("184.75.209.130:6060")).willReturn(Collections.singletonList(new HttpCookie("test", "test")));

        fbFetcher = new FbFetcher(accountRepository, webDriverRepository);
    }

    @Test
    public void shouldReturnValidResponse() {
        // then
        assertThat(fbFetcher.get("any"), is("Valid html"));
    }
}
