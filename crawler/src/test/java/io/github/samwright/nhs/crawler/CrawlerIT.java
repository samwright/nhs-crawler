package io.github.samwright.nhs.crawler;

import io.github.samwright.nhs.common.crawler.CrawlerClient;
import io.github.samwright.nhs.common.pages.PagesClient;
import io.github.samwright.nhs.common.search.SearchClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {"maxPagesPerCrawl = 10", "randomiseTmpDir=true", "deleteOnExit=true",
                "eureka.client.enabled=false", "feign.hystrix.enabled=false",
                "crawler-app.ribbon.listOfServers:localhost:${server.port}"},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(clients = {CrawlerClient.class, SearchClient.class})
@DirtiesContext
public class CrawlerIT {
    @Autowired
    private CrawlerClient crawlerClient;

    @Autowired
    private SearchClient searchClient;

    @MockBean
    private PagesClient pagesClient;

    @Test
    public void testClient() throws Exception {
        assertThat(crawlerClient.status().getRunningUrlCount()).isEqualTo(-1);
        assertThat(crawlerClient.start()).isEqualTo("crawler started");

        // It can take a while for all the crawler threads to shut down
        await().atMost(50, TimeUnit.SECONDS).until(() -> !crawlerClient.status().isRunning());

        int urlCount = (int) crawlerClient.status().getRunningUrlCount();
        assertThat(urlCount).isBetween(8, 10);
        verify(pagesClient, atLeast(8)).create(any());
    }
}
