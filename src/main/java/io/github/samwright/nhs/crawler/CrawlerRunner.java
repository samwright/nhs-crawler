package io.github.samwright.nhs.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.inject.Provider;

@Component
public class CrawlerRunner {
    @Autowired
    private Provider<Crawler> crawlerProvider;

    @Autowired
    private Provider<CrawlController> controllerProvider;

    private CrawlController controller;

    @Value("${crawlerCount:5}") @Setter
    private int crawlerCount;

    private CrawlerStatus status = new CrawlerStatus();

    public void run() {
        // Create a new controller
        controller = controllerProvider.get();

        // Reset status
        status.setStarted();
        try {
            // Start crawler
            controller.start(crawlerProvider::get, crawlerCount);
        } catch (Exception e) {
            // Swallow any exception and store it in the status
            status.setException(e);
        } finally {
            // Update the status that it has stopped
            status.setStopped();
        }
    }

    @PreDestroy
    public void stop() {
        if (controller != null) {
            controller.shutdown();
            controller.waitUntilFinish();
        }
    }

    public CrawlerStatus getStatus() {
        return status
                .setRunningUrlCount(controller == null ? -1 : controller.getFrontier().getNumberOfProcessedPages());
    }
}
