package io.github.samwright.nhs.crawler;

import lombok.Data;

@Data
public class CrawledPage {
    private String title;
    private String url;
    private String content;
}
