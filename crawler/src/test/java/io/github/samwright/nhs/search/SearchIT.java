package io.github.samwright.nhs.search;

import io.github.samwright.nhs.common.search.SearchClient;
import io.github.samwright.nhs.util.DirHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = {"randomiseTmpDir=true", "deleteOnExit=true",
                "eureka.client.enabled=false",
                "searcher.ribbon.listOfServers:localhost:${local.server.port}"},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchIT {
    private static final String ADDISONS_SYMPTOMS = "http://www.nhs.uk/Conditions/Addisons-disease/Pages/Symptoms.aspx";
    private static final String ADDISONS_TREATMENT = "http://www.nhs.uk/Conditions/Addisons-disease/Pages/Treatment.aspx";
    private static final String ANEURYSM_SYMPTOMS = "http://www.nhs.uk/Conditions/Aneurysm/Pages/Symptoms.aspx";
    private static final String ANEURYSM_TREATMENT = "http://www.nhs.uk/Conditions/Aneurysm/Pages/Treatment.aspx";

    @Autowired
    private SearchClient searchClient;

    @Autowired
    private DirHelper dirHelper;

    @Before
    public void setUp() throws Exception {
        loadCrawledPage("1.json");
        loadCrawledPage("2.json");
        loadCrawledPage("3.json");
        loadCrawledPage("4.json");
        assertThat(searchClient.reindex()).isEqualTo("now reindexing");
        Thread.sleep(1000);
        while (searchClient.status().isRunning()) {
            Thread.sleep(1000);
        }
    }

    private void loadCrawledPage(String filename) throws IOException {
        Files.copy(ClassLoader.getSystemResourceAsStream(filename),
                   dirHelper.getDataDir().resolve("pages").resolve(filename));
    }

    @Test(timeout = 20000)
    public void testSearch() throws Exception {
        assertThat(searchClient.search("symptoms of Addison's")).isEqualTo(ADDISONS_SYMPTOMS);
        assertThat(searchClient.search("treatment for Addisons'")).isEqualTo(ADDISONS_TREATMENT);
        assertThat(searchClient.search("symptom of aneurysms")).isEqualTo(ANEURYSM_SYMPTOMS);
        assertThat(searchClient.search("treatments for aneurysm")).isEqualTo(ANEURYSM_TREATMENT);
        assertThat(searchClient.search("antidisestablishmentarianism")).isEqualTo("No match found");
    }
}
