package ru.wobot.sm.proxy.service.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component
public class RoundRobinSharedFbCookieRepository implements AccountRepository {
    private static final int GET_TIMEOUT = 20;

    private final HazelcastInstance hazelcastInstance;
    private final ObjectMapper objectMapper;

    @Override
    public Collection<HttpCookie> getCookies(String proxy) {
        IQueue<Collection<String>> queue = this.hazelcastInstance.getQueue(proxy);
        Collection<HttpCookie> result = new ArrayList<>(queue.size());

        Collection<String> cookies = null;
        try {
            cookies = queue.poll(GET_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (cookies == null)
            throw new IllegalStateException("No cookies for proxy [" + proxy + "].");

        for (String cookieString : cookies) {
            JsonNode cookieNode;
            try {
                cookieNode = objectMapper.readValue(cookieString, JsonNode.class);
            } catch (IOException e) {
                throw new IllegalStateException("Couldn't deserialize cookie from distributed queue.", e);
            }
            HttpCookie cookie = new HttpCookie(cookieNode.get("name").asText(), cookieNode.get("value").asText());
            cookie.setDomain(cookieNode.get("domain").asText());
            cookie.setVersion(0); // for toString to generate only name value pairs
            result.add(cookie);
        }
        queue.add(cookies);

        return result;
    }

    @Autowired
    public RoundRobinSharedFbCookieRepository(Collection<JsonNode> logins, HazelcastInstance hazelcastInstance,
                                              ObjectMapper objectMapper) {
        this.hazelcastInstance = hazelcastInstance;
        this.objectMapper = objectMapper;

        ISet<Collection<String>> unique = this.hazelcastInstance.getSet("unique");
        ILock lock = this.hazelcastInstance.getLock("lock");

        for (JsonNode node : logins) {
            IQueue<Collection<String>> queue = this.hazelcastInstance.getQueue(node.get("proxy").get("address").asText());
            for (JsonNode n : node.get("cookies")) {
                Collection<String> cookies = new ArrayList<>();
                for (JsonNode cookieNode : n) {
                    cookies.add(cookieNode.toString());
                }

                lock.lock();
                try {
                    if (unique.add(cookies))
                        queue.add(cookies);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

}
