package io.github.samwright.nhs.common.pages;

import lombok.Data;

import java.util.List;

@Data
public class PageBatch {
    private List<Page> pages;
    private String nextResult;
    private boolean isLast;
}
