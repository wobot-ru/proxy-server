package ru.wobot.sm.proxy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import ru.wobot.sm.proxy.service.FbProfileService;
import ru.wobot.sm.proxy.service.fetch.Fetcher;
import ru.wobot.sm.proxy.service.uri.UriResolver;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTests {
    @Autowired
    private TestRestTemplate restTemplate;

    /*@MockBean
    private UriResolver uriResolver;*/

    /*@MockBean
    private Fetcher fetcher;*/

    @Before
    public void setup() {
        //given(uriResolver.resolve("appScopedId")).willReturn("realId");
        //given(fetcher.get(FbProfileService.FACEBOOK_URI + "/100004451677809")).willReturn("Full html");
    }

    @Test
    public void test() throws Exception {
        ResponseEntity<String> profile = restTemplate.getForEntity("/facebook/548469171978134", String.class);
        assertThat(profile.getBody(), containsString("Наталья"));
    }

}
