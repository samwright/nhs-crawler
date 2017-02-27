package io.github.samwright.nhs.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrawledPageDaoTest {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private Path pagesDir;

    @Mock
    private DirHelper dirHelper;

    @InjectMocks
    private CrawledPageDao dao;

    @Before
    public void setUp() throws Exception {
        pagesDir = tempDir.getRoot().toPath();
        when(dirHelper.createSubFolder("pages")).thenReturn(pagesDir);
        dao.init();
    }

    @Test
    public void testWrite() throws Exception {
        CrawledPage page = new CrawledPage()
                .setContent("content").setUrl("http://www.nhs.uk/Some%20Thing").setTitle("title");
        dao.write(page);
        assertThat(pagesDir.resolve("http_www_nhs_uk_some_thing.json").toFile())
                .as("crawler file should exist and contain page information in the expected format")
                .exists().isFile().hasContent(new ObjectMapper().writeValueAsString(page));
    }

    @Test
    public void testReadAllPages() throws Exception {
        Set<CrawledPage> pages = Sets.newHashSet(
                new CrawledPage().setContent("c1").setTitle("t1").setUrl("u1"),
                new CrawledPage().setContent("c2").setTitle("t2").setUrl("u2"));
        for (CrawledPage page : pages) {
            dao.write(page);
        }
        assertThat(dao.readAllPages().collect(Collectors.toSet())).isEqualTo(pages);
    }
}
