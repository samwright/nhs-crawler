package io.github.samwright.nhs.search;

import com.google.common.collect.Lists;
import io.github.samwright.nhs.crawler.CrawledPageDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.io.IOException;

/**
 * Class to index crawled pages using Apache Lucene.
 */
@Component
@Slf4j
public class PageIndexer {

    private final IndexingStatus status = new IndexingStatus();

    @Autowired
    private Provider<SearchHelper> searchHelperProvider;

    @Autowired
    private CrawledPageDao crawledPageDao;

    /**
     * Index all crawled pages.
     */
    public void index() {
        try (SearchHelper searchHelper = searchHelperProvider.get()) {
            log.info("Starting to index");
            status.setStarted();

            // Delete all docs from the index
            searchHelper.deleteAllDocs();

            // Add all crawled pages to the index
            searchHelper.addDocumentsToIndex(crawledPageDao.readAllPages()
                    .map(page -> Lists.newArrayList(
                            new StringField(SearchConfig.URL_FIELD, page.getUrl(), Field.Store.YES),
                            new TextField(SearchConfig.TITLE_FIELD, page.getTitle(), Field.Store.YES),
                            new TextField(SearchConfig.CONTENT_FIELD, page.getContent(), Field.Store.NO))));
            log.info("Finished indexing");
        } catch (Exception e) {
            status.setException(e);
        } finally {
            status.setStopped();
        }
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
