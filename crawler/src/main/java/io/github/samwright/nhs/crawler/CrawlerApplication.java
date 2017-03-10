package io.github.samwright.nhs.crawler;

import io.github.samwright.nhs.common.CommonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CommonConfig.class)
public class CrawlerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }
}
