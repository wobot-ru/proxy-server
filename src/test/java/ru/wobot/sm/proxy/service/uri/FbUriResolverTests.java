package ru.wobot.sm.proxy.service.uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.wobot.sm.proxy.service.repository.AccountRepository;
import ru.wobot.sm.proxy.service.repository.ProxyRepository;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/*@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestApplication.class)*/
public class FbUriResolverTests {
    MockRestServiceServer mockServer;

    @Mock
    private ProxyRepository proxyRepository;

    @Mock
    private AccountRepository accountRepository;

    private UriResolver uriResolver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        uriResolver = new FbUriResolver(requestFactory, restTemplate,
                this.proxyRepository, this.accountRepository);

        mockServer = MockRestServiceServer.createServer(restTemplate);

        given(proxyRepository.getHost()).willReturn("184.75.209.130:6060");
        given(proxyRepository.getCredentials("184.75.209.130:6060")).willReturn("test:testpassword");
    }

    @Test
    public void shouldReturnRealIdForUserWithPhoto() throws URISyntaxException {
        // given
        String appScopedId = "testId";
        mockServer.expect(requestTo(FbUriResolver.FACEBOOK_API_URI + "/" + appScopedId + "/picture"))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withSuccess()
                        .location(new URI("https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/13319748_964300080358306_4359820039606630570_n.jpg?oh=679a22c4ca408058f19cf5fd4af88b27&oe=57C89341")));

        mockServer.expect(requestTo(FbUriResolver.FACEBOOK_URI + "/" + "13319748_964300080358306"))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withSuccess()
                        .location(new URI("https://www.facebook.com/photo.php?fbid=964300080358306&set=a.110279489093707.13075.100003349701954&type=3&theater")));

        // when
        String id = uriResolver.resolve(appScopedId);

        // then
        assertThat(id, is("100003349701954"));

        mockServer.verify();
    }

    @Test
    public void shouldReturnRealScreenNameForUserWithNoPhoto() throws URISyntaxException {
        // given
        String appScopedId = "testId";
        this.mockServer.expect(requestTo(FbUriResolver.FACEBOOK_API_URI + "/" + appScopedId + "/picture"))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withSuccess()
                        .location(new URI("https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/13332710_1222314591152433_7967765849351173229_n.jpg?oh=146f282c8235e8892568eadd1c73a592&oe=57D1FD91")));

        this.mockServer.expect(requestTo(FbUriResolver.FACEBOOK_URI + "/" + "13332710_1222314591152433"))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        this.mockServer.expect(requestTo(FbUriResolver.FACEBOOK_URI + "/app_scoped_user_id/" + appScopedId))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withSuccess()
                        .location(new URI("https://www.facebook.com/asemkin")));

        // when
        String id = uriResolver.resolve(appScopedId);

        // then
        assertThat(id, is("asemkin"));
        mockServer.verify();
    }

    @Test
    public void shouldReturnRealIdForUserWithDefaultPhoto() throws URISyntaxException {
        // given
        String appScopedId = "testId";
        this.mockServer.expect(requestTo(FbUriResolver.FACEBOOK_API_URI + "/" + appScopedId + "/picture"))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withSuccess()
                        .location(new URI("https://scontent.xx.fbcdn.net/v/t1.0-1/c15.0.50.50/p50x50/1379841_10150004552801901_469209496895221757_n.jpg?oh=41eda0af5152a6d5673b221f24b4c2a4&oe=57C7B633")));

        this.mockServer.expect(requestTo(FbUriResolver.FACEBOOK_URI + "/" + "1379841_10150004552801901"))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withSuccess()
                        .location(new URI("https://www.facebook.com/photo.php?fbid=10150004552801901&set=a.1001968110775.1364293.499829591&type=3&theater")));

        this.mockServer.expect(requestTo(FbUriResolver.FACEBOOK_URI + "/app_scoped_user_id/" + appScopedId))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withSuccess()
                        .location(new URI("https://www.facebook.com/profile.php?id=100004451677809")));

        // when
        String id = uriResolver.resolve(appScopedId);

        // then
        assertThat(id, is("100004451677809"));
        mockServer.verify();
    }

    @Test
    public void shouldReturnRealScreenNameWODotsForAppScopedId() throws URISyntaxException {
        // given (same for test purpose)
        String appScopedId = "testId";
        this.mockServer.expect(requestTo(FbUriResolver.FACEBOOK_API_URI + "/" + appScopedId + "/picture"))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withSuccess()
                        .location(new URI("https://scontent.xx.fbcdn.net/v/t1.0-1/p50x50/13332710_1222314591152433_7967765849351173229_n.jpg?oh=146f282c8235e8892568eadd1c73a592&oe=57D1FD91")));

        this.mockServer.expect(requestTo(FbUriResolver.FACEBOOK_URI + "/" + "13332710_1222314591152433"))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        this.mockServer.expect(requestTo(FbUriResolver.FACEBOOK_URI + "/app_scoped_user_id/" + appScopedId))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withSuccess()
                        .location(new URI("https://www.facebook.com/renata.davidova.50")));

        // when
        String id = uriResolver.resolve(appScopedId);

        // then
        assertThat(id, is("renatadavidova50"));
        mockServer.verify();
    }

}
