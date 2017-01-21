package io.github.samwright.nhs.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
@Slf4j
public class Crawler extends WebCrawler {
    private static final Predicate<String> PATH_FILTER = Pattern.compile("/(c|C)onditions/.*\\.aspx.*").asPredicate();
    private static final Predicate<String> IS_INDEX_PAGE = Pattern.compile("/(c|C)onditions/(p|P)ages.*").asPredicate();

    @Autowired
    private CrawledPageWriter pageWriter;

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        return url.getDomain().equals("www.nhs.uk") && PATH_FILTER.test(url.getPath());
    }

    @Override
    public void visit(Page page) {
        // Skip index pages
        if (IS_INDEX_PAGE.test(page.getWebURL().getPath())) {
            log.debug("Skipping index page: {}", page.getWebURL());
            return;
        }

        // Skip non-html pages
        if (!page.getContentType().startsWith("text/html")) {
            log.debug("Skipping non-html ({}) page: {}", page.getContentType(), page.getWebURL());
            return;
        }

        // Write page to file (as json)
        try {
            Document doc = Jsoup.parse(new String(page.getContentData()));
            pageWriter.write(new CrawledPage()
                    .setUrl(page.getWebURL().toString())
                    .setContent(doc.text())
                    .setTitle(doc.title()));
            log.info("Crawler visited: {}", page.getWebURL());
        } catch (IOException e) {
            log.warn("Problem writing page {}", page.getWebURL(), e);
        }
    }
}
