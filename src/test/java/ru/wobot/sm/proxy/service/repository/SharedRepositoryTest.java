package ru.wobot.sm.proxy.service.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Hazelcast;
import org.junit.After;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Collection;

public class SharedRepositoryTest {
    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected static Collection<JsonNode> logins;

    @BeforeClass
    public static void initOnce() throws IOException {
        logins = objectMapper.readValue(RoundRobinSharedFbCookieRepositoryTest.class.getResource("/cookies.json"), new TypeReference<Collection<JsonNode>>() {
        });
    }

    @After
    public void cleanup() throws Exception {
        Hazelcast.shutdownAll();
    }
}
