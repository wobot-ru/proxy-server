package ru.wobot.sm.proxy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wobot.sm.proxy.domain.Profile;
import ru.wobot.sm.proxy.service.fetch.Fetcher;
import ru.wobot.sm.proxy.service.uri.UriResolver;

@Service
public class FbProfileService implements ProfileService {
    private final UriResolver resolver;
    private final Fetcher fetcher;

    public static final String FACEBOOK_URI = "https://www.facebook.com";

    @Autowired
    public FbProfileService(UriResolver resolver, Fetcher fetcher) {
        this.resolver = resolver;
        this.fetcher = fetcher;
    }

    @Override
    public Profile getProfile(String appScopedId) {
        String id = resolver.resolve(appScopedId);
        return new Profile(id, appScopedId, fetcher.get(FACEBOOK_URI + "/" + id));
    }
}
