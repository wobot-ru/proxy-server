package ru.wobot.sm.proxy.service;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import ru.wobot.sm.proxy.service.fetch.Fetcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class FbSearchServiceTest {

    private FbSearchService searchService;

    @Mock
    private Fetcher fetcher;

    @Before
    public void setUp() throws Exception {
        String searchData = IOUtils.toString(FbSearchServiceTest.class.getResource("/facebook/search/search.html"));
        given(this.fetcher.get("https://www.facebook.com/search/latest/?q=%D1%82%D0%B5%D0%BB%D0%B52", 0, 0))
                .willReturn(searchData);

        searchService = new FbSearchService(fetcher);
    }

    @Test
    public void responseNotNull() throws Exception {
        String resp = searchService.getLatest("теле2", 0, 0);
        assertThat(resp, notNullValue());
    }
}
