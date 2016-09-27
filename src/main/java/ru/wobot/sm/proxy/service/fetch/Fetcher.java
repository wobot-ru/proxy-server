package ru.wobot.sm.proxy.service.fetch;

public interface Fetcher {
    String get(String url);
    String get(String url, int numberOfLoadPages, int maxPageHeight);
}
