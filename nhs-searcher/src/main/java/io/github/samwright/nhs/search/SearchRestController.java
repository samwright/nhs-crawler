package io.github.samwright.nhs.search;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.inject.Provider;

import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.samwright.nhs.common.search.IndexingStatus;
import io.github.samwright.nhs.common.search.SearchResult;

@RestController
@RequestMapping("/api/search")
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

    @RequestMapping("/reindex")
    public String reindex() throws IOException {
        if (indexer.getStatus().isRunning()) {
            return "already reindexing";
        } else {
            executor.submit(indexer::recreateIndex);
            return "now reindexing";
        }
    }

    @GetMapping("/status")
    public IndexingStatus status() throws IOException {
        return indexer.getStatus();
    }

    @GetMapping("/multiple")
    public List<SearchResult> searchMultiple(@RequestParam(name = "q") String query) throws IOException, ParseException {
        try (SearchHelper searchHelper = searchHelperProvider.get()) {
            return searchHelper.search(query);
        }
    }

    @GetMapping
    public String search(@RequestParam(name = "q") String query) throws IOException, ParseException {
        return searchMultiple(query).stream()
                .findFirst()
                .map(r -> r.getFields().get(SearchConfig.URL_FIELD))
                .orElse("No match found");
    }
}
