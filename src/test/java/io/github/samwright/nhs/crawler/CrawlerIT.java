package io.github.samwright.nhs.crawler;

import io.github.samwright.nhs.search.IndexingStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {"maxPagesPerCrawl = 10", "randomiseTmpDir=true", "deleteOnExit=true"},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CrawlerIT {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test(timeout = 120000)
    public void testCrawling() throws Exception {
        assertThat(getStatus().getRunningUrlCount()).isEqualTo(-1);
        assertThat(restTemplate.getForObject("/crawler/start", String.class)).isEqualTo("crawler started");
        Thread.sleep(1000);
        while (getStatus().isRunning()) {
            Thread.sleep(1000);
        }
        assertThat(getStatus().getRunningUrlCount()).isBetween(8L, 10L);

        // Wait for the index to be automatically updated
        while (restTemplate.getForObject("/search/status", IndexingStatus.class).getSize() == 0) {
            Thread.sleep(1000);
        }
    }

    private CrawlerStatus getStatus() {
        return restTemplate.getForObject("/crawler/status", CrawlerStatus.class);
    }
}
