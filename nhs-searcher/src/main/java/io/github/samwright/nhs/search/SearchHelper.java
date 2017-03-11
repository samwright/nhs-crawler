package io.github.samwright.nhs.search;

import io.github.samwright.nhs.common.search.SearchResult;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class for using Apache Lucene to build and search its index.
 * <p/>
 * This handles closing any {@link IndexSearcher} objects that were used, by
 * calling {@link #close()}.
 * <p/>
 * NB. The first time you perform a read operation {@link #getIndexSize()} or
 * {@link #search(String)}) the state of the index is saved, and all
 * future read operations will use the same index state. As such, performing
 * a read operation, a write operation ({@link #addDocumentsToIndex(Stream)}),
 * then the same read operation will read the same results twice, regardless of
 * the documents added in the write operation.
 * <p/>
 * To read from the new state of the index, close this helper and open a new
 * one.
 */
@Component
@Scope("prototype")
public class SearchHelper implements Closeable, AutoCloseable {
    public static final int MAX_RESULTS = 10;

    @Autowired @Setter
    private SearcherManager searcherManager;

    @Autowired @Setter
    private IndexWriter indexWriter;

    @Autowired @Setter
    private QueryParser queryParser;

    private IndexSearcher indexSearcher;

    private boolean documentsChanged = false;

    /**
     * Search the index using the given query, and return the ${@value #MAX_RESULTS} top results.
     *
     * @param query the query. Special search characters ? and * will be removed from the query
     * @return the ${@value #MAX_RESULTS} top results.
     * @throws IOException if there was a problem reading the index.
     * @throws ParseException if there was a problem parsing the query.
     */
    public List<SearchResult> search(String query) throws IOException, ParseException {
        TopDocs topDocs = getSearcher().search(queryParser.parse(query.replaceAll("(\\*|\\?)", "")), MAX_RESULTS);
        return Arrays.stream(topDocs.scoreDocs)
                .map(this::getResult)
                .collect(Collectors.toList());
    }

    /**
     * @return the size of the index.
     * @throws IOException if there was a problem reading the index.
     */
    public int getIndexSize() throws IOException {
        return getSearcher().getIndexReader().numDocs();
    }

    /**
     * Delete all docs in the index.
     *
     * @throws IOException if there was a problem accessing the index.
     */
    public void deleteAllDocs() throws IOException {
        documentsChanged = true;
        indexWriter.deleteAll();
    }

    /**
     * @param docs the stream of docs to add to the index.
     * @throws IOException
     */
    public void addDocumentsToIndex(Stream<Iterable<? extends IndexableField>> docs) throws IOException {
        documentsChanged = true;
        docs.forEach(this::addDocumentToIndex);
    }

    /**
     * Close any {@link IndexSearcher} objects that were used.
     * <p/>
     * NB. the {@link SearcherManager} and {@link IndexWriter} are singletons
     * and are only closed when the application closes (for performance).
     *
     * @throws IOException if there was a problem closing the index searcher.
     */
    @Override
    public void close() throws IOException {
        if (indexSearcher != null) {
            searcherManager.release(indexSearcher);
        }
        if (documentsChanged) {
            indexWriter.commit();
            searcherManager.maybeRefresh();
        }
    }

    @SneakyThrows
    private void addDocumentToIndex(Iterable<? extends IndexableField> doc) {
        indexWriter.addDocument(doc);
    }

    @SneakyThrows
    private SearchResult getResult(ScoreDoc scoreDoc) {
        Document doc = indexSearcher.doc(scoreDoc.doc);
        Map<String, String> fields = doc.getFields().stream()
                .filter(f -> f.stringValue() != null)
                .collect(Collectors.toMap(IndexableField::name, IndexableField::stringValue));
        return new SearchResult().setScore(scoreDoc.score).setFields(fields);
    }

    private IndexSearcher getSearcher() throws IOException {
        if (indexSearcher == null) {
            indexSearcher = searcherManager.acquire();
        }
        return indexSearcher;
    }
}
