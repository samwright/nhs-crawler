package io.github.samwright.nhs.itest;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import io.github.samwright.nhs.common.crawler.CrawlerStatus;
import io.github.samwright.nhs.common.search.IndexingStatus;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CrawlAndSearchIT {

    // todo: update docker-compose-rule when they next release and remove waitingForService call
    @Rule
    public DockerComposeRule docker = DockerComposeRule.builder()
            .file("target/test-classes/docker-compose.yml")
            .waitingForService("eureka", HealthChecks.toRespond2xxOverHttp(
                    8761, p -> p.inFormat("http://localhost:$EXTERNAL_PORT/eureka/apps/NHS-SERVICE")))
            .build();

    private RestTemplate restTemplate;
    private String uri;

    @Before
    public void setUp() throws Exception {
        uri = docker.containers().container("crawler").port(8080).inFormat("http://localhost:$EXTERNAL_PORT");
        restTemplate = new RestTemplateBuilder().rootUri(uri).build();
    }

    @Test
    public void testCrawlAndSearch() throws Exception {
        // Crawl until at least 10 sites have been retrieved
        assertThat(restTemplate.getForObject("/crawler/start", String.class)).isEqualTo("crawler started");
        Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> getCrawlerStatus().getRunningUrlCount() > 10);

        // Reindex
        assertThat(restTemplate.getForObject("/search/reindex", String.class)).isEqualTo("now reindexing");
        Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> getIndexerStatus().getSize() > 0);

        // Search for something that will definitely be in the index
        String searchResult = search("nhs");
        assertThat(searchResult).startsWith("http://www.nhs.uk");
    }

    private CrawlerStatus getCrawlerStatus() {
        return restTemplate.getForObject("/crawler/status", CrawlerStatus.class);
    }

    private IndexingStatus getIndexerStatus() {
        return restTemplate.getForObject("/search/status", IndexingStatus.class);
    }

    private String search(String query) throws UnsupportedEncodingException {
        return restTemplate.getForObject("/search?q=" + URLEncoder.encode(query, "UTF-8"), String.class);
    }
}
