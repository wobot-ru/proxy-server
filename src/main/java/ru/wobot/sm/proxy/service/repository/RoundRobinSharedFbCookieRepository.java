package ru.wobot.sm.proxy.service.repository;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.HttpCookie;
import java.util.Collection;

@Component
public class RoundRobinSharedFbCookieRepository implements AccountRepository {
    @Autowired
    HazelcastInstance hazelcastInstance;

    @Override
    public Collection<HttpCookie> getCookies(String proxy) {
        return null;
    }
}
