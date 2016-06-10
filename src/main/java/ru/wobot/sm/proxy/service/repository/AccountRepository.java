package ru.wobot.sm.proxy.service.repository;

import java.net.HttpCookie;
import java.util.Collection;

public interface AccountRepository {
    Collection<HttpCookie> getCookies(String proxy);
}
