package ru.wobot.sm.proxy.service.repository;

import org.springframework.stereotype.Component;

@Component
public class RoundRobinSharedProxyRepository implements ProxyRepository {
    @Override
    public String getHost() {
        return "184.75.209.130:6060";
    }

    @Override
    public String getCredentials(String host) {
        return "snt@wobot.co:PfYZ7J(b<^<[rhm";
    }
}
