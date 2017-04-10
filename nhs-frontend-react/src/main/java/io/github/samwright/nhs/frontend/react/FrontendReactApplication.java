package io.github.samwright.nhs.frontend.react;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.github.samwright.nhs.common.CommonConfig;

@SpringBootApplication
@Import(CommonConfig.class)
public class FrontendReactApplication
{
    public static void main(String[] args) {
        SpringApplication.run(FrontendReactApplication.class, args);
    }
}
