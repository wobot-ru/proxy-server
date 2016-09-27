package ru.wobot.sm.proxy.service;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ru.wobot.sm.proxy.domain.SearchResponse;
import ru.wobot.sm.proxy.service.fetch.Fetcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class FbSearchServiceTest {

    private FbSearchService searchService;

    @MockBean
    private Fetcher fetcher;

    @Before
    public void setUp() throws Exception {
        String searchData = IOUtils.toString(FbSearchServiceTest.class.getResource("/facebook/search/search.html"));
        given(this.fetcher.get("https://www.facebook.com/search/str/%D1%82%D0%B5%D0%BB%D0%B52/stories-keyword/this-week/date/stories/intersect"))
                .willReturn(searchData);

        searchService = new FbSearchService(fetcher);
    }

    @Test
    public void responseNotNull() throws Exception {
        SearchResponse resp = searchService.search("теле2", 0 ,0);
        assertThat(resp, notNullValue());
    }

    @Test
    public void responseContainsPosts() throws Exception {
        SearchResponse resp = searchService.search("теле2", 0 ,0);
        assertThat(resp.getPosts(), notNullValue());
    }


}
