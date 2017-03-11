package io.github.samwright.nhs.search;

import com.google.common.collect.Lists;
import io.github.samwright.nhs.common.pages.Page;
import io.github.samwright.nhs.common.pages.PageBatch;
import io.github.samwright.nhs.common.pages.PagesClient;
import io.github.samwright.nhs.common.search.IndexingStatus;
import org.apache.lucene.index.IndexableField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageIndexerTest {
    private static final int DOCS_COUNT = 123;

    @Mock
    private Provider<SearchHelper> searchHelperProvider;

    @Mock
    private PagesClient pagesClient;

    @InjectMocks
    private PageIndexer indexer;

    @Mock
    private SearchHelper searchHelper;

    @Captor
    private ArgumentCaptor<Stream<Iterable<? extends IndexableField>>> docsCaptor;

    private Page page = new Page().setUrl("URL").setTitle("TITLE").setContent("CONTENT");

    private LocalDateTime halfwayThroughIndexing;

    @Before
    public void setUp() throws Exception {
        when(pagesClient.read()).thenReturn(new PageBatch().setPages(Lists.newArrayList(page)).setLast(true));
        when(searchHelperProvider.get()).thenReturn(searchHelper);
        when(searchHelper.getIndexSize()).thenReturn(DOCS_COUNT);
        indexer.setIndexIntervalSeconds(1);
        indexer.init();
    }

    @After
    public void tearDown() throws Exception {
        indexer.destroy();
    }

    @Test
    public void testIndex() throws Exception {
        indexer.recreateIndex();

        InOrder inOrder = inOrder(searchHelper);
        inOrder.verify(searchHelper).deleteAllDocs();
        inOrder.verify(searchHelper).addDocumentsToIndex(docsCaptor.capture());
        inOrder.verify(searchHelper).close();
        checkIndexedDocIsPage();
    }

    private void checkIndexedDocIsPage() {
        Iterable<? extends IndexableField> doc = docsCaptor.getValue().findFirst().get();
        assertThat(doc).hasSize(3);
        assertThat(doc).filteredOn("name", SearchConfig.URL_FIELD)
                .extracting("fieldsData")
                .containsOnly(page.getUrl());
        assertThat(doc).filteredOn("name", SearchConfig.TITLE_FIELD)
                .extracting("fieldsData")
                .containsOnly(page.getTitle());
        assertThat(doc).filteredOn("name", SearchConfig.CONTENT_FIELD)
                .extracting("fieldsData")
                .containsOnly(page.getContent());
    }

    @Test
    public void testIndexException() throws Exception {
        IOException exception = new IOException();
        doThrow(exception).when(searchHelper).deleteAllDocs();

        indexer.recreateIndex();

        verify(searchHelper).close();
        IndexingStatus status = indexer.getStatus();
        assertThat(status.getException()).isEqualTo(exception);
        assertThat(status.isRunning()).isFalse();
    }

    @Test
    public void testGetStatusBeforeIndexing() throws Exception {
        IndexingStatus status = indexer.getStatus();

        InOrder inOrder = inOrder(searchHelper);
        inOrder.verify(searchHelper).getIndexSize();
        inOrder.verify(searchHelper).close();

        assertThat(status.getStartTime()).isNull();
        assertThat(status.getStopTime()).isNull();
        assertThat(status.getException()).isNull();
        assertThat(status.getSize()).isEqualTo(DOCS_COUNT);
        assertThat(status.isRunning()).isFalse();
    }

    @Test
    public void testGetStatusAfterIndexing() throws Exception {
        doAnswer(inv -> {
            assertThat(indexer.getStatus().isRunning()).isTrue();
            halfwayThroughIndexing = nowWithPadding();
            return null;
        }).when(searchHelper).addDocumentsToIndex(any());

        LocalDateTime beforeIndexing = nowWithPadding();
        indexer.recreateIndex();
        LocalDateTime afterIndexing = nowWithPadding();
        IndexingStatus status = indexer.getStatus();

        assertThat(status.getException()).isNull();
        assertThat(status.getSize()).isEqualTo(DOCS_COUNT);
        assertThat(LocalDateTime.parse(status.getStartTime())).isBetween(beforeIndexing, halfwayThroughIndexing);
        assertThat(LocalDateTime.parse(status.getStopTime())).isBetween(halfwayThroughIndexing, afterIndexing);
        assertThat(status.isRunning()).isFalse();
    }

    private LocalDateTime nowWithPadding() throws InterruptedException {
        Thread.sleep(1);
        LocalDateTime now = LocalDateTime.now();
        Thread.sleep(1);
        return now;
    }

    @Test
    public void testIndexSoon() throws Exception {
        indexer.indexSoon(page);
        verify(searchHelper, timeout(5000).atLeastOnce()).addDocumentsToIndex(docsCaptor.capture());
        checkIndexedDocIsPage();
    }

    @Test
    public void testIndexSoonExceptionIsSwallowed() throws Exception {
        // Real-time indexing is done on a best-efforts basis, and we don't want an errant page
        // stopping the indexing from running, so the exception is swallowed and logged.
        doThrow(new IOException()).when(searchHelper).addDocumentsToIndex(docsCaptor.capture());
        indexer.indexSoon(page);
        Thread.sleep(100);
    }
}
