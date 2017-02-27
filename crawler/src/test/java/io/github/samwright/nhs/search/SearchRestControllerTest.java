package io.github.samwright.nhs.search;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.samwright.nhs.common.search.IndexingStatus;
import io.github.samwright.nhs.common.search.SearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchRestControllerTest {
    private static final String FIRST_URL = "first", SECOND_URL = "second", QUERY = "some query";

    @Mock
    private PageIndexer indexer;

    @Mock
    private Provider<SearchHelper> searchHelperProvider;

    @InjectMocks
    private SearchRestController controller;

    @Mock
    private SearchHelper searchHelper;

    private IndexingStatus status = new IndexingStatus();

    private int indexCount = 0;

    private SearchResult firstResult = new SearchResult().setFields(ImmutableMap.of("url", FIRST_URL));
    private SearchResult secondResult = new SearchResult().setFields(ImmutableMap.of("url", SECOND_URL));

    @Before
    public void setUp() throws Exception {
        status.setStartTime("a long time ago");
        when(indexer.getStatus()).thenReturn(status);
        doAnswer(inv -> ++indexCount).when(indexer).recreateIndex();
        when(searchHelperProvider.get()).thenReturn(searchHelper);
        when(searchHelper.search(QUERY)).thenReturn(Lists.newArrayList(firstResult, secondResult));
    }

    @After
    public void tearDown() throws Exception {
        controller.destroy();
    }

    @Test
    public void testIndex() throws Exception {
        assertThat(controller.reindex()).isEqualTo("now reindexing");
        Thread.sleep(100);
        assertThat(indexCount).isEqualTo(1);
    }

    @Test
    public void testAlreadyIndexing() throws Exception {
        status.setRunning(true);
        assertThat(controller.reindex()).isEqualTo("already reindexing");
        Thread.sleep(100);
        assertThat(indexCount).isEqualTo(0);
    }

    @Test
    public void testStatus() throws Exception {
        assertThat(controller.status()).isSameAs(status);
    }

    @Test
    public void testSearchMultiple() throws Exception {
        assertThat(controller.searchMultiple(QUERY)).containsExactly(firstResult, secondResult);
    }

    @Test
    public void testSearch() throws Exception {
        assertThat(controller.search(QUERY)).isEqualTo(FIRST_URL);
    }
}
