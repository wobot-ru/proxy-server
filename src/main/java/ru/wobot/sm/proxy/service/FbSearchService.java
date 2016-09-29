package ru.wobot.sm.proxy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wobot.sm.proxy.service.fetch.Fetcher;

import java.io.IOException;
import java.net.URLEncoder;

@Service
public class FbSearchService implements SearchService {
    private static final Logger logger = LoggerFactory.getLogger(FbSearchService.class);
    private final Fetcher fetcher;
    private static final String FACEBOOK_SEARCH_LATEST_URI = "https://www.facebook.com/search/latest/?q=";

    @Autowired
    public FbSearchService(Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public String getLatest(String query, int numberOfLoadPages, int maxPageHeight) {
        try {
            String baseUrl = FACEBOOK_SEARCH_LATEST_URI + URLEncoder.encode(query, "UTF-8").replace('+', ' ');
            return fetcher.get(baseUrl, numberOfLoadPages, maxPageHeight);
        } catch (IOException e) {
            logger.error("query=" + query, e);
        }
        return null;
    }
}