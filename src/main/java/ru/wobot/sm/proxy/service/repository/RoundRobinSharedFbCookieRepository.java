package ru.wobot.sm.proxy.service.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import com.hazelcast.map.listener.EntryEvictedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RoundRobinSharedFbCookieRepository implements AccountRepository, EntryEvictedListener<String, Collection<String>> {
    private static final Logger logger = LoggerFactory
            .getLogger(RoundRobinSharedFbCookieRepository.class);
    private static final int GET_TIMEOUT = 20;

    private final HazelcastInstance hazelcastInstance;
    private final ObjectMapper objectMapper;
    private IMap<String, Collection<String>> delayMap;

    @Override
    public Collection<HttpCookie> getCookies(String proxy) {
        IQueue<Collection<String>> queue = this.hazelcastInstance.getQueue(proxy);
        List<HttpCookie> result = new ArrayList<>(queue.size());
        Collection<String> cookies = null;

        logger.info("Getting cookies for proxy: [" + proxy + "]");
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
        logger.info("Got cookies for proxy: [" + proxy + "]; user ID used: [" + result.get(2).getValue() + "], delaying it.");
        delayMap.put(proxy + "/" + result.get(2).getValue(), cookies);

        return result;
    }

    @Autowired
    public RoundRobinSharedFbCookieRepository(Collection<JsonNode> logins, HazelcastInstance hazelcastInstance,
                                              ObjectMapper objectMapper) {
        this.hazelcastInstance = hazelcastInstance;
        delayMap = this.hazelcastInstance.getMap("delayMap");
        delayMap.addEntryListener(this, true);

        this.objectMapper = objectMapper;

        ISet<Collection<String>> unique = this.hazelcastInstance.getSet("unique");
        ILock lock = this.hazelcastInstance.getLock("lock");

        for (JsonNode node : logins) {
            IQueue<Collection<String>> queue = this.hazelcastInstance.getQueue(node.get("proxy").get("address").asText());
            for (JsonNode n : node.get("cookies")) {
                Collection<String> cookies = new ArrayList<>();
                for (JsonNode cookieNode : n)
                    cookies.add(cookieNode.toString());

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

    @Override
    public void entryEvicted(EntryEvent<String, Collection<String>> event) {
        logger.info("Cookies expired for proxy: [" + event.getKey().split("/")[0] + "], user ID: [" + ((List<String>) event.getOldValue()).get(2));
        IQueue<Collection<String>> queue = this.hazelcastInstance.getQueue(event.getKey().split("/")[0]);
        queue.add(event.getOldValue());
    }

}
