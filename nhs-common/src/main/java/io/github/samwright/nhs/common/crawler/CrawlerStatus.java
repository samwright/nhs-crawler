package io.github.samwright.nhs.common.crawler;

import io.github.samwright.nhs.common.util.JobStatus;
import lombok.Data;

@Data
public class CrawlerStatus extends JobStatus {
    private long runningUrlCount;
}
