package io.github.samwright.nhs.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.samwright.nhs.common.crawler.CrawlerClient;
import io.github.samwright.nhs.common.pages.Page;
import io.github.samwright.nhs.common.pages.PageBatch;
import io.github.samwright.nhs.common.pages.PagesClient;
import io.github.samwright.nhs.common.search.SearchClient;
import io.github.samwright.nhs.common.util.DirHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {"randomiseTmpDir=true", "deleteOnExit=true",
                "eureka.client.enabled=false", "feign.hystrix.enabled=false",
                "crawler-app.ribbon.listOfServers:localhost:${server.port}"},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(clients = {CrawlerClient.class, SearchClient.class})
@DirtiesContext
public class SearchIT {
    private static final String ADDISONS_SYMPTOMS = "http://www.nhs.uk/Conditions/Addisons-disease/Pages/Symptoms.aspx";
    private static final String ADDISONS_TREATMENT = "http://www.nhs.uk/Conditions/Addisons-disease/Pages/Treatment.aspx";
    private static final String ANEURYSM_SYMPTOMS = "http://www.nhs.uk/Conditions/Aneurysm/Pages/Symptoms.aspx";
    private static final String ANEURYSM_TREATMENT = "http://www.nhs.uk/Conditions/Aneurysm/Pages/Treatment.aspx";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SearchClient searchClient;

    @Autowired
    private DirHelper dirHelper;

    @MockBean
    private PagesClient pagesClient;

    @Before
    public void setUp() throws Exception {
        PageBatch batch = new PageBatch()
                .setLast(true)
                .setPages(Arrays.asList(loadCrawledPage("1.json"),
                                        loadCrawledPage("2.json"),
                                        loadCrawledPage("3.json"),
                                        loadCrawledPage("4.json")));

        doReturn(batch).when(pagesClient).read();
//        when(pagesClient.read()).thenReturn(batch).thenThrow(new RuntimeException("Should not read pages twice"));
        Thread.sleep(1000);
        assertThat(searchClient.reindex()).isEqualTo("now reindexing");
        await().atMost(10, TimeUnit.SECONDS).until(() -> searchClient.status().getSize() > 0);
    }

    private Page loadCrawledPage(String filename) throws IOException {
        return objectMapper.readValue(ClassLoader.getSystemResourceAsStream(filename), Page.class);
    }

    @Test
    public void testSearch() throws Exception {
        assertThat(searchClient.search("symptoms of Addison's")).isEqualTo(ADDISONS_SYMPTOMS);
        assertThat(searchClient.search("treatment for Addisons'")).isEqualTo(ADDISONS_TREATMENT);
        assertThat(searchClient.search("symptom of aneurysms")).isEqualTo(ANEURYSM_SYMPTOMS);
        assertThat(searchClient.search("treatments for aneurysm")).isEqualTo(ANEURYSM_TREATMENT);
        assertThat(searchClient.search("antidisestablishmentarianism")).isEqualTo("No match found");
    }
}
