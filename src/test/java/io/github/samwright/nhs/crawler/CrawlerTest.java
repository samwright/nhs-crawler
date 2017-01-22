package io.github.samwright.nhs.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrawlerTest {
    private static final String TITLE = "This is a title";
    private static final String OTHER_TEXT = "And here is some text";
    private static final String CONTENT = "<html><title>" + TITLE + "</title>" + OTHER_TEXT;
    private static final String ALL_TEXT = TITLE + " " + OTHER_TEXT;
    private static final String URL = "http://www.nhs.uk/conditions/something.aspx";

    @Mock
    private CrawledPageDao pageDao;

    @InjectMocks
    private Crawler crawler;

    @Mock
    private Page page;

    @Before
    public void setUp() throws Exception {
        when(page.getWebURL()).thenReturn(newUrl("http://www.nhs.uk/conditions/something.aspx"));
        when(page.getContentType()).thenReturn("text/html; charset=utf-8");
        when(page.getContentData()).thenReturn(CONTENT.getBytes());
    }

    private WebURL newUrl(String urlString) {
        WebURL url = new WebURL();
        url.setURL(urlString);
        return url;
    }

    @Test
    public void testShouldVisit() throws Exception {
        assertThat(crawler.shouldVisit(null, newUrl("http://www.nhs.uk/conditions/something.aspx")))
                .as("the crawler should visit a valid condition page").isTrue();
        assertThat(crawler.shouldVisit(null, newUrl("http://www.nhs.uk/Conditions/something.aspx")))
                .as("the crawler should visit a valid Condition (with a capital C) page").isTrue();
        assertThat(crawler.shouldVisit(null, newUrl("http://www.nhs.uk/conditions/something.aspx?tabname=TabName")))
                .as("the crawler should visit a valid condition sub page").isTrue();
        assertThat(crawler.shouldVisit(null, newUrl("http://www.nhs.uk/not/conditions")))
                .as("the crawler should only visit conditions pages").isFalse();
        assertThat(crawler.shouldVisit(null, newUrl("http://notthenhs.com")))
                .as("the crawler should only visit the NHS domain").isFalse();
    }

    @Test
    public void testVisit() throws Exception {
        crawler.visit(page);
        CrawledPage expected = new CrawledPage().setContent(ALL_TEXT).setTitle(TITLE).setUrl(URL);
        verify(pageDao).write(expected);
    }

    @Test
    public void testVisitIndexPage() throws Exception {
        when(page.getWebURL()).thenReturn(newUrl("http://www.nhs.uk/conditions/Pages"));
        crawler.visit(page);
        verify(pageDao, never()).write(any());
    }

    @Test
    public void testVisitNonHtmlPage() throws Exception {
        when(page.getContentType()).thenReturn("application/pdf");
        crawler.visit(page);
        verify(pageDao, never()).write(any());
    }

    @Test
    public void testVisitButCannotWritePage() throws Exception {
        // Exception swallowed and logged so crawl can continue
        doThrow(new IOException()).when(pageDao).write(any());
        crawler.visit(page);
    }
}
