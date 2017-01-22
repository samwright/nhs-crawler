package io.github.samwright.nhs.search;

import io.github.samwright.nhs.util.DirHelper;
import org.apache.lucene.index.IndexWriter;
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
public class SearchConfigTest {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Mock
    private DirHelper dirHelper;

    @InjectMocks
    private SearchConfig searchConfig;

    @Before
    public void setUp() throws Exception {
        when(dirHelper.createSubFolder("index")).thenReturn(tempDir.getRoot().toPath());
    }

    @Test
    public void testAnalyzer() throws Exception {
        assertThat(searchConfig.analyzer()).isNotNull();
    }

    @Test
    public void testIndexWriter() throws Exception {
        assertThat(searchConfig.indexWriter()).isNotNull();
    }

    @Test
    public void testSearcherManager() throws Exception {
        assertThat(searchConfig.searcherManager()).isNotNull();
    }

    @Test
    public void testQueryParser() throws Exception {
        assertThat(searchConfig.queryParser()).isNotNull();
    }
}
