package ru.wobot.sm.proxy.driver;

import org.openqa.selenium.WebDriver;

public interface ProxiedWebDriver {
    String getProxy();

    WebDriver getWebDriver();
}
