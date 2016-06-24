package ru.wobot.sm.proxy.service.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISet;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class RoundRobinSharedProxyRepository implements ProxyRepository {
    private final HazelcastInstance hazelcastInstance;

    public RoundRobinSharedProxyRepository(Collection<JsonNode> logins, HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;

        ISet<String> unique = this.hazelcastInstance.getSet("uniqueProxy");
        ILock lock = this.hazelcastInstance.getLock("lockProxy");
        IQueue<String> queue = this.hazelcastInstance.getQueue("proxy");

        for (JsonNode node : logins) {
            lock.lock();
            try {
                String proxy = node.get("proxy").get("address").asText();
                if (unique.add(proxy))
                    queue.add(proxy);
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public String getHost() {
        IQueue<String> queue = hazelcastInstance.getQueue("proxy");
        String proxy = queue.poll();
        if (proxy == null)
            throw new IllegalStateException("No proxies found in shared queue.");
        queue.add(proxy);

        return proxy;
    }

    @Override
    public String getCredentials(String host) {
        return "snt@wobot.co:PfYZ7J(b<^<[rhm";
    }
}
