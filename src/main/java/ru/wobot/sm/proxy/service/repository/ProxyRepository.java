package ru.wobot.sm.proxy.service.repository;

public interface ProxyRepository {
    String getHost();

    String getCredentials(String host);
}
