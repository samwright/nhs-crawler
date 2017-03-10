package io.github.samwright.nhs.common;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:common.properties"})
@AutoConfigureBefore(FeignConfig.class)
@ComponentScan
public class CommonConfig {
}
