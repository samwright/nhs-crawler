package io.github.samwright.nhs.search;

import com.google.common.collect.ImmutableMap;
import io.github.samwright.nhs.common.util.DirHelper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class SearchConfig {
    public static final String URL_FIELD = "url", TITLE_FIELD = "title", CONTENT_FIELD = "content";
    public static final float TITLE_SEARCH_WEIGHTING = 1.2f, CONTENT_SEARCH_WEIGHTING = 1f;

    @Autowired
    private DirHelper dirHelper;

    @Bean(destroyMethod = "close")
    public Analyzer analyzer() {
        return new EnglishAnalyzer();
    }

    @Bean(destroyMethod = "close")
    public Directory directory() throws IOException {
        return FSDirectory.open(dirHelper.createSubFolder("index"));
    }

    @Bean(destroyMethod = "close")
    public IndexWriter indexWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer())
                .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(directory(), config);
    }

    @Bean(destroyMethod = "close")
    public SearcherManager searcherManager() throws IOException {
        return new SearcherManager(indexWriter(), new SearcherFactory());
    }

    @Bean
    public QueryParser queryParser() {
        return new MultiFieldQueryParser(
                new String[]{TITLE_FIELD, CONTENT_FIELD},
                analyzer(),
                ImmutableMap.of(
                        TITLE_FIELD, TITLE_SEARCH_WEIGHTING,
                        CONTENT_FIELD, CONTENT_SEARCH_WEIGHTING));
    }
}
