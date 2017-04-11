package io.github.samwright.nhs.common.search;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "nhs-searcher-app", path = "/api/search")
public interface SearchClient {
    @RequestMapping("/reindex")
    String reindex();

    @RequestMapping("/status")
    IndexingStatus status();

    @RequestMapping("/multiple")
    List<SearchResult> searchMultiple(@RequestParam(name = "q") String query);

    @RequestMapping()
    String search(@RequestParam(name = "q") String query);
}
