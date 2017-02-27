package io.github.samwright.nhs.crawler;

import io.github.samwright.nhs.util.JobStatus;
import lombok.Data;

@Data
public class CrawlerStatus extends JobStatus {
    private long runningUrlCount;
}
