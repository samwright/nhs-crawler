package io.github.samwright.nhs.crawler;

import lombok.Data;

@Data
public class CrawlerStatus {
    private String startTime, stopTime;
    private boolean isRunning;
    private long runningUrlCount;
    private Exception exception;
}
