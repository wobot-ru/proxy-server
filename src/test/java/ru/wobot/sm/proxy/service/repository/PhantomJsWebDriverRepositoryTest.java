package ru.wobot.sm.proxy.service.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.wobot.sm.proxy.driver.ProxiedWebDriver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;

public class PhantomJsWebDriverRepositoryTest {
    @Mock
    private ProxyRepository proxyRepository;

    private ProxiedWebDriver proxiedWebDriver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        given(proxyRepository.getHost()).willReturn("184.75.209.130:6060");
        given(proxyRepository.getCredentials("184.75.209.130:6060")).willReturn("snt@wobot.co:PfYZ7J(b<^<[rhm");
        WebDriverRepository phantomJsWebDriverRepository = new PhantomJsWebDriverRepository(proxyRepository);
        proxiedWebDriver = phantomJsWebDriverRepository.getDriver();
    }

    @After
    public void cleanup() throws Exception {
        proxiedWebDriver.getWebDriver().quit();
    }

    @Test
    public void shouldReturnValidDriverWithProxyHost() {
        // then
        assertThat(proxiedWebDriver.getProxy(), is("184.75.209.130:6060"));
    }

}
