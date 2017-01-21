package io.github.samwright.nhs.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import io.github.samwright.nhs.util.DirHelper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class CrawlerConfig {
    @Value("${maxPagesPerCrawl:-1}") @Setter
    private int maxPagesPerCrawl;

    @Autowired
    private DirHelper dirHelper;

    @Bean
    @Scope("prototype")
    public CrawlController crawlController() throws Exception {
        CrawlConfig config = new CrawlConfig();
        config.setMaxPagesToFetch(maxPagesPerCrawl);
        config.setCrawlStorageFolder(dirHelper.createSubFolder("crawl").toString());
        config.setResumableCrawling(true);
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        controller.addSeed("http://www.nhs.uk/Conditions/Pages/hub.aspx");
        return controller;
    }
}
