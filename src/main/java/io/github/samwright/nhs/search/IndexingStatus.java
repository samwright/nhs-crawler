package io.github.samwright.nhs.search;

import io.github.samwright.nhs.util.JobStatus;
import lombok.Data;

@Data
public class IndexingStatus extends JobStatus {
    private int size;
}
