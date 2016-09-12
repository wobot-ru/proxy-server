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
    public static final String FACEBOOK_URI_PREFIX = "https://www.facebook.com/search/str/";
    public static final String FACEBOOK_URI_SUFFIX = "/stories-keyword/this-week/date/stories/intersect";

    @Autowired
    public FbSearchService(Fetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public SearchResponse search(String query) {
        try {
            String data = fetcher.get(FACEBOOK_URI_PREFIX + URLEncoder.encode(query, "UTF-8") + FACEBOOK_URI_SUFFIX);

            Path file = Paths.get("c:\\tmp\\search.txt");
            Files.write(file, data.getBytes(Charset.forName("UTF-8")));

            return new SearchResponse(data, null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}