package io.github.samwright.nhs.search;

import io.github.samwright.nhs.common.CommonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CommonConfig.class)
public class SearcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearcherApplication.class, args);
    }
}
