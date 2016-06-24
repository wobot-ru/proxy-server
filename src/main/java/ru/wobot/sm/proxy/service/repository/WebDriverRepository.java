package ru.wobot.sm.proxy.service.repository;

import org.openqa.selenium.WebDriver;
import ru.wobot.sm.proxy.driver.ProxiedWebDriver;

public interface WebDriverRepository {
    ProxiedWebDriver getDriver();
}
