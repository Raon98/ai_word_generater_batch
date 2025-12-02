package com.ai.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gpt")
public record GptConfig(String secretKey) {
}
