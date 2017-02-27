package io.github.samwright.nhs.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import io.github.samwright.nhs.common.util.DirHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawlerConfigTest {
    private static final int MAX_PAGES_PER_CRAWL = 3;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Mock
    private DirHelper dirHelper;

    @InjectMocks
    private CrawlerConfig config;

    @Before
    public void setUp() throws Exception {
        when(dirHelper.createSubFolder("crawl")).thenReturn(tempDir.getRoot().toPath());
        config.setMaxPagesPerCrawl(MAX_PAGES_PER_CRAWL);
    }

    @Test
    public void testCrawlController() throws Exception {
        CrawlController controller = config.crawlController();
        assertThat(controller.getConfig().getMaxPagesToFetch()).isEqualTo(MAX_PAGES_PER_CRAWL);
        assertThat(controller.getConfig().getCrawlStorageFolder()).isEqualTo(tempDir.getRoot().toPath().toString());
        assertThat(controller.getConfig().isResumableCrawling()).isTrue();
    }
}
