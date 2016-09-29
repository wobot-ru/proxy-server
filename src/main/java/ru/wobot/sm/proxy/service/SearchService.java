package ru.wobot.sm.proxy.service;

public interface SearchService {
    String getLatest(String query, int numberOfLoadPages, int maxPageHeight);
}
