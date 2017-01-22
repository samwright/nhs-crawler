package io.github.samwright.nhs.search;

import lombok.Data;

import java.util.Map;

@Data
public class SearchResult {
    private float score;
    private Map<String, String> fields;
}
