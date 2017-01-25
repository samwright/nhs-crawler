package io.github.samwright.nhs.search;

import com.google.common.collect.Lists;
import io.github.samwright.nhs.crawler.CrawledPage;
import io.github.samwright.nhs.crawler.CrawledPageDao;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Provider;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Class to index crawled pages using Apache Lucene.
 */
@Component
@Slf4j
public class PageIndexer {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final IndexingStatus status = new IndexingStatus();

    @Autowired
    private Provider<SearchHelper> searchHelperProvider;

    @Autowired
    private CrawledPageDao crawledPageDao;

    @Value("${indexIntervalSeconds:10}") @Setter
    private int indexIntervalSeconds;

    private final ConcurrentLinkedQueue<CrawledPage> cachedPagesToIndex = new ConcurrentLinkedQueue<>();

    @PostConstruct
    public void init() {
        executor.scheduleWithFixedDelay(this::flushCachedPagesToIndex, 0, indexIntervalSeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        executor.shutdown();
    }

    private void flushCachedPagesToIndex() {
        int pagesToAdd = cachedPagesToIndex.size();
        if (pagesToAdd > 0) {
            try (SearchHelper searchHelper = searchHelperProvider.get()) {
                searchHelper.addDocumentsToIndex(
                        Stream.generate(cachedPagesToIndex::poll)
                                .limit(pagesToAdd)
                                .map(this::createDoc));
                log.debug("Added {} pages to the index", pagesToAdd);
            } catch (IOException e) {
                log.error("Could not index recently-crawled pages", e);
            }
        }
    }

    /**
     * @param page the page to add to the index soon.
     */
    public void indexSoon(CrawledPage page) {
        cachedPagesToIndex.add(page);
    }

    /**
     * Index all crawled pages.
     */
    public void recreateIndex() {
        try (SearchHelper searchHelper = searchHelperProvider.get()) {
            log.info("Starting to index");
            status.setStarted();

            // Delete all docs from the index
            searchHelper.deleteAllDocs();

            // Add all crawled pages to the index
            searchHelper.addDocumentsToIndex(crawledPageDao.readAllPages().map(this::createDoc));
            log.info("Finished indexing");
        } catch (Exception e) {
            status.setException(e);
        } finally {
            status.setStopped();
        }
    }

    private List<? extends IndexableField> createDoc(CrawledPage page) {
        return Lists.newArrayList(
                new StringField(SearchConfig.URL_FIELD, page.getUrl(), Field.Store.YES),
                new TextField(SearchConfig.TITLE_FIELD, page.getTitle(), Field.Store.YES),
                new TextField(SearchConfig.CONTENT_FIELD, page.getContent(), Field.Store.NO));
    }

    /**
     * @return the status of the indexer.
     * @throws IOException if there is a problem
     */
    public IndexingStatus getStatus() throws IOException {
        try (SearchHelper searchHelper = searchHelperProvider.get()) {
            return status.setSize(searchHelper.getIndexSize());
        }
    }
}
