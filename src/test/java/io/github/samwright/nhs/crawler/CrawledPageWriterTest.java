package io.github.samwright.nhs.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.samwright.nhs.util.DirHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawledPageWriterTest {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private Path pagesDir;

    @Mock
    private DirHelper dirHelper;

    @InjectMocks
    private CrawledPageWriter writer;

    @Before
    public void setUp() throws Exception {
        pagesDir = tempDir.getRoot().toPath();
        when(dirHelper.createSubFolder("pages")).thenReturn(pagesDir);
        writer.init();
    }

    @Test
    public void testWrite() throws Exception {
        CrawledPage page = new CrawledPage()
                .setContent("content").setUrl("http://www.nhs.uk/Some%20Thing").setTitle("title");
        writer.write(page);
        assertThat(pagesDir.resolve("http_www_nhs_uk_some_thing.json").toFile())
                .as("crawler file should exist and contain page information in the expected format")
                .exists().isFile().hasContent(new ObjectMapper().writeValueAsString(page));
    }
}
