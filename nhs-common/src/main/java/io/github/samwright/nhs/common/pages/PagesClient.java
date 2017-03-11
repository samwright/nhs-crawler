package io.github.samwright.nhs.common.pages;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "nhs-pages-app", path = "/page")
public interface PagesClient {
    @RequestMapping(method = RequestMethod.PUT)
    void create(@RequestBody Page page);

    @RequestMapping
    PageBatch readNext(@RequestParam String nextResult);

    @RequestMapping
    PageBatch read();
}
