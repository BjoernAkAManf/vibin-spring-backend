package com.vibinofficial.backend.hasura;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vibin.backend.hasura")
public class HasuraConfig {
    private String host;
}
