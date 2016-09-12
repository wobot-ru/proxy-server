package ru.wobot.sm.proxy.service;

import ru.wobot.sm.proxy.domain.SearchResponse;

public interface SearchService {
    SearchResponse search(String query);
}
