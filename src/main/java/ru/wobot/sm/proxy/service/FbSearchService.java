package ru.wobot.sm.proxy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wobot.sm.proxy.domain.SearchResponse;
import ru.wobot.sm.proxy.service.fetch.Fetcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FbSearchService implements SearchService {
    private final Fetcher fetcher;
    public static final String FACEBOOK_URI_PREFIX = "https://www.facebook.com/search/latest/?q=";
    public static final String FACEBOOK_URI_SUFFIX = "";

    @Autowired
    public FbSearchService(Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public SearchResponse search(String query, int numberOfLoadPages, int maxPageHeight) {
        try {
            String baseUrl = FACEBOOK_URI_PREFIX + URLEncoder.encode(query, "UTF-8") + FACEBOOK_URI_SUFFIX;
            String data = fetcher.get(baseUrl, numberOfLoadPages, maxPageHeight);

//            Path file = Paths.get("c:\\tmp\\search_big.txt");
//            Files.write(file, data.getBytes(Charset.forName("UTF-8")));

            return new SearchResponse(data, null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}