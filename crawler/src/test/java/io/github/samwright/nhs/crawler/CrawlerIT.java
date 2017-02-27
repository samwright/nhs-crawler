package io.github.samwright.nhs.crawler;

import io.github.samwright.nhs.common.crawler.CrawlerClient;
import io.github.samwright.nhs.common.search.SearchClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {"maxPagesPerCrawl = 10", "randomiseTmpDir=true", "deleteOnExit=true",
                "eureka.client.enabled=false",
                "crawler.ribbon.listOfServers:localhost:${local.server.port}",
                "searcher.ribbon.listOfServers:localhost:${local.server.port}"},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CrawlerIT {
    @Autowired
    private CrawlerClient crawlerClient;

    @Autowired
    private SearchClient searchClient;

    @Test
    public void testClient() throws Exception {
        assertThat(crawlerClient.status().getRunningUrlCount()).isEqualTo(-1);
        assertThat(crawlerClient.start()).isEqualTo("crawler started");
        Thread.sleep(1000);
        while (crawlerClient.status().isRunning()) {
            Thread.sleep(1000);
        }
        assertThat(crawlerClient.status().getRunningUrlCount()).isBetween(8L, 10L);

        // Wait for the index to be automatically updated
        while (searchClient.status().getSize() == 0) {
            Thread.sleep(1000);
        }
    }
}
