package io.github.samwright.nhs.search;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * The Apache Lucene classes are either final or have final methods, making
 * mockito-based testing difficult. All Lucene calls are done in SearchHelper,
 * so this test will perform a classical unit test.
 */
public class SearchHelperTest {
    private static final String SEARCHED_FIELD = "a", IGNORED_FIELD = "b";

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private SearcherManager searcherManager;
    private IndexWriter indexWriter;
    private QueryParser queryParser;
    private Analyzer analyzer;

    @Before
    public void setUp() throws Exception {
        Directory dir = FSDirectory.open(tempDir.getRoot().toPath());
        analyzer = new EnglishAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer).setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        indexWriter = new IndexWriter(dir, config);
        searcherManager = new SearcherManager(indexWriter, new SearcherFactory());
        queryParser = new QueryParser(SEARCHED_FIELD, analyzer);
    }

    private SearchHelper createSearchHelper() {
        SearchHelper searchHelper = new SearchHelper();
        searchHelper.setSearcherManager(searcherManager);
        searchHelper.setIndexWriter(indexWriter);
        searchHelper.setQueryParser(queryParser);
        return searchHelper;
    }

    @Test
    public void testIndexAndSearch() throws Exception {
        try (SearchHelper helper = createSearchHelper()) {
            assertThat(helper.getIndexSize()).isEqualTo(0);
            assertThat(helper.search("1")).isEmpty();

            helper.addDocumentsToIndex(Stream.of(
                    Lists.newArrayList(
                            new StringField(SEARCHED_FIELD, "1", Field.Store.YES),
                            new StringField(IGNORED_FIELD, "2", Field.Store.YES)),
                    Lists.newArrayList(
                            new StringField(SEARCHED_FIELD, "2", Field.Store.YES),
                            new StringField(IGNORED_FIELD, "1", Field.Store.YES))));

            assertThat(helper.getIndexSize())
                    .as("check this helper is still reading from the initial (empty) state of the index")
                    .isEqualTo(0);
        }

        // Open a new helper to be able to read the new state of the index.
        try (SearchHelper helper = createSearchHelper()) {
            assertThat(helper.getIndexSize()).isEqualTo(2);
            SearchResult result = Iterables.getOnlyElement(helper.search("1"));
            assertThat(result.getFields()).containsExactly(entry(SEARCHED_FIELD, "1"), entry(IGNORED_FIELD, "2"));

            // Delete all docs
            helper.deleteAllDocs();
        }

        // Open a new helper to be able to read the new (empty) state of the index.
        try (SearchHelper helper = createSearchHelper()) {
            assertThat(helper.getIndexSize()).as("check all docs in the index have been deleted").isEqualTo(0);
        }
    }


}
