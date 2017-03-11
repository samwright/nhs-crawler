package io.github.samwright.nhs.crawler;

import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import io.github.samwright.nhs.common.pages.Page;
import io.github.samwright.nhs.common.pages.PagesClient;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
@Scope("prototype")
@Slf4j
public class Crawler extends WebCrawler {
    private static final Predicate<String> PATH_FILTER = Pattern.compile("/(c|C)onditions/.*\\.aspx.*").asPredicate();
    private static final Predicate<String> IS_INDEX_PAGE = Pattern.compile("/(c|C)onditions/(p|P)ages.*").asPredicate();

    @Autowired
    private PagesClient pagesClient;


    @Override
    public boolean shouldVisit(edu.uci.ics.crawler4j.crawler.Page referringPage, WebURL url) {
        return url.getDomain().equals("www.nhs.uk") && PATH_FILTER.test(url.getPath());
    }

    @Override
    public void visit(edu.uci.ics.crawler4j.crawler.Page page) {
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

        try {
            Document doc = Jsoup.parse(new String(page.getContentData()));
            Page crawledPage = new Page()
                    .setUrl(page.getWebURL().toString())
                    .setContent(doc.text())
                    .setTitle(doc.title());

            // Store the page
            pagesClient.create(crawledPage);

            log.info("Crawler visited: {}", page.getWebURL());
        } catch (Exception e) {
            log.warn("Problem writing page {}", page.getWebURL(), e);
        }
    }
}
