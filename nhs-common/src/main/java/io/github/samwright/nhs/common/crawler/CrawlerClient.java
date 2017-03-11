package io.github.samwright.nhs.common.crawler;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "nhs-crawler-app", path = "/crawler")
public interface CrawlerClient {
    @RequestMapping("/start")
    String start();

    @RequestMapping("/stop")
    String stop();

    @RequestMapping("/status")
    CrawlerStatus status();
}
