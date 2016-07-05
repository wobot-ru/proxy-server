package ru.wobot.sm.proxy.service.repository;

import com.hazelcast.core.Hazelcast;
import org.junit.Test;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RoundRobinSharedFbCookieRepositoryTest extends SharedRepositoryTest {

    @Test
    public void shouldReturnCookiesFromFile() {
        // given
        String proxy = "184.75.209.130:6060";
        AccountRepository accountRepository = new RoundRobinSharedFbCookieRepository(logins, Hazelcast.newHazelcastInstance(), objectMapper);

        // when
        Collection<HttpCookie> cookies = accountRepository.getCookies(proxy);

        // then
        assertThat(((List<HttpCookie>) cookies).get(2).getValue(), is("100010008910833"));  // user id
    }

    @Test
    public void shouldReturnNextCookiesFromFile() {
        // given
        String proxy = "184.75.209.130:6060";
        AccountRepository accountRepository = new RoundRobinSharedFbCookieRepository(logins, Hazelcast.newHazelcastInstance(), objectMapper);

        // when
        accountRepository.getCookies(proxy);
        Collection<HttpCookie> cookies = accountRepository.getCookies(proxy); // second cookie set

        // then
        assertThat(((List<HttpCookie>) cookies).get(2).getValue(), is("100009927522883")); // user id
    }

    @Test
    public void shouldReturnFirstCookiesFromFileAgain() {
        // given
        String proxy = "184.75.208.10:6060";
        AccountRepository accountRepository = new RoundRobinSharedFbCookieRepository(logins, Hazelcast.newHazelcastInstance(), objectMapper);

        // when
        accountRepository.getCookies(proxy);
        accountRepository.getCookies(proxy);
        Collection<HttpCookie> cookies = accountRepository.getCookies(proxy); // first again

        // then
        assertThat(((List<HttpCookie>) cookies).get(2).getValue(), containsString("10001")); // user id.
    }

    @Test
    public void shouldReturnDifferentCookiesFromDifferentInstances() {
        // given
        String proxy = "184.75.209.130:6060";
        AccountRepository accountRepository = new RoundRobinSharedFbCookieRepository(logins, Hazelcast.newHazelcastInstance(), objectMapper);
        AccountRepository secondAccountRepository = new RoundRobinSharedFbCookieRepository(logins, Hazelcast.newHazelcastInstance(), objectMapper);

        // when
        Collection<HttpCookie> firstCookies = accountRepository.getCookies(proxy); // first cookie set
        Collection<HttpCookie> secondCookies = secondAccountRepository.getCookies(proxy); // second cookie set
        String firstId = ((List<HttpCookie>) firstCookies).get(2).getValue();
        String secondId = ((List<HttpCookie>) secondCookies).get(2).getValue();

        // then
        assertThat(firstId, is(not(equalTo(secondId))));
    }

    @Test
    public void shouldReturnDifferentCookiesFromDifferentInstancesConcurrently() throws InterruptedException {
        // given
        String proxy = "184.75.208.10:6060";
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(2);
        List<String> ids = Collections.synchronizedList(new ArrayList<>(2));

        for (int i = 0; i < 2; i++) {
            new Thread() {
                public void run() {
                    try {
                        start.await();
                        try {
                            AccountRepository ar = new RoundRobinSharedFbCookieRepository(logins, Hazelcast.newHazelcastInstance(), objectMapper);
                            ids.add(((List<HttpCookie>) ar.getCookies(proxy)).get(2).getValue());
                        } finally {
                            finish.countDown();
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
            }.start();
        }

        // when
        start.countDown();
        finish.await();

        // then
        assertThat(ids.get(0), is("100010064499022"));
        assertThat(ids.get(1), is("100010198925638"));
    }

    /*@Test
    public void shouldNotReturnCookieAgainIfItIsNotExpired() {
        // given
        String proxy = "184.75.209.130:6060";
        AccountRepository accountRepository = new RoundRobinSharedFbCookieRepository(logins, Hazelcast.newHazelcastInstance(), objectMapper);

        // when
        Collection<HttpCookie> firstCookies = accountRepository.getCookies(proxy); // first cookie set

        String firstId = ((List<HttpCookie>) firstCookies).get(2).getValue();

        // then
        //assertThat(firstId, is(not(equalTo(secondId))));
    }*/

}
