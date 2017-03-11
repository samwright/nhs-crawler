package io.github.samwright.nhs.common.search;

import io.github.samwright.nhs.common.util.JobStatus;
import lombok.Data;

@Data
public class IndexingStatus extends JobStatus {
    private int size;
}
