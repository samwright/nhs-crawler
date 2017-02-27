package io.github.samwright.nhs.search;

import io.github.samwright.nhs.common.search.IndexingStatus;
import io.github.samwright.nhs.common.search.SearchResult;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import javax.inject.Provider;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class SearchRestController {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    private PageIndexer indexer;

    @Autowired
    private Provider<SearchHelper> searchHelperProvider;

    @PreDestroy
    public void destroy() {
        executor.shutdown();
    }

    @RequestMapping("/search/reindex")
    public String reindex() throws IOException {
        if (indexer.getStatus().isRunning()) {
            return "already reindexing";
        } else {
            executor.submit(indexer::recreateIndex);
            return "now reindexing";
        }
    }

    @RequestMapping("/search/status")
    public IndexingStatus status() throws IOException {
        return indexer.getStatus();
    }

    @RequestMapping("/search/multiple")
    public List<SearchResult> searchMultiple(@RequestParam(name = "q") String query) throws IOException, ParseException {
        try (SearchHelper searchHelper = searchHelperProvider.get()) {
            return searchHelper.search(query);
        }
    }

    @RequestMapping("/search")
    public String search(@RequestParam(name = "q") String query) throws IOException, ParseException {
        return searchMultiple(query).stream()
                .findFirst()
                .map(r -> r.getFields().get(SearchConfig.URL_FIELD))
                .orElse("No match found");
    }
}
