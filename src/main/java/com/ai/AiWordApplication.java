package com.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@ConfigurationPropertiesScan
@PropertySource(value = "classpath:properties/env.properties", ignoreResourceNotFound = true)
public class AiWordApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiWordApplication.class, args);
    }

}
