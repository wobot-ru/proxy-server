package ru.wobot.sm.proxy.service.repository;

import com.hazelcast.core.Hazelcast;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RoundRobinSharedProxyRepositoryTest extends SharedRepositoryTest {
    private ProxyRepository proxyRepository = new RoundRobinSharedProxyRepository(logins, Hazelcast.newHazelcastInstance());

    @Test
    public void shouldReturnProxyFromFile() {
        // given
        String expected = "184.75.209.130:6060";

        // when
        String proxy = proxyRepository.getHost();

        // then
        assertThat(proxy, is(IsEqual.equalTo(expected)));
    }

    @Test
    public void shouldReturnNextProxyFromFile() {
        // given
        String expected = "184.75.208.10:6060";
        ProxyRepository secondRepository = new RoundRobinSharedProxyRepository(logins, Hazelcast.newHazelcastInstance());

        // when
        this.proxyRepository.getHost();
        String proxy = secondRepository.getHost();

        // then
        assertThat(proxy, is(IsEqual.equalTo(expected)));
    }

    @Test
    public void shouldReturnFirstProxyAgain() {
        // given
        String expected = "184.75.209.130:6060";
        ProxyRepository secondRepository = new RoundRobinSharedProxyRepository(logins, Hazelcast.newHazelcastInstance());

        // when
        this.proxyRepository.getHost();
        secondRepository.getHost();
        String proxy = this.proxyRepository.getHost();

        // then
        assertThat(proxy, is(IsEqual.equalTo(expected)));
    }

}
