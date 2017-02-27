package io.github.samwright.nhs.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.github.samwright.nhs.common.pages.Page;
import io.github.samwright.nhs.common.pages.PageBatch;
import io.github.samwright.nhs.common.util.DirHelper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PagesRestControllerTest {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private Path pagesDir;

    @Mock
    private DirHelper dirHelper;

    @InjectMocks
    private PagesRestController controller;

    @Before
    public void setUp() throws Exception {
        pagesDir = tempDir.getRoot().toPath();
        when(dirHelper.createSubFolder("pages")).thenReturn(pagesDir);
        controller.init();
    }

    @Test
    public void testWrite() throws Exception {
        Page page = new Page()
                .setContent("content").setUrl("http://www.nhs.uk/Some%20Thing").setTitle("title");
        controller.create(page);
        assertThat(pagesDir.resolve("http_www_nhs_uk_some_thing.json").toFile())
                .as("crawler file should exist and contain page information in the expected format")
                .exists().isFile().hasContent(new ObjectMapper().writeValueAsString(page));
    }

    @Test
    public void testReadAllPages() throws Exception {
        List<Page> pages = Lists.newArrayList(
                new Page().setContent("c1").setTitle("t1").setUrl("u1"),
                new Page().setContent("c2").setTitle("t2").setUrl("u2"));
        for (Page page : pages) {
            controller.create(page);
        }

        PageBatch batch = controller.read(Optional.empty(), Optional.empty());

        Assertions.assertThat(batch.getPages()).isEqualTo(pages);
        Assertions.assertThat(batch.isLast()).isTrue();
        Assertions.assertThat(batch.getNextResult()).isNull();

    }
}
