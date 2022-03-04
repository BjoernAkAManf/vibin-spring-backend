package com.vibinofficial.backend.avatars.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("services.minio")
public class Config {
    private String host;
    private String accessKey;
    private String secretKey;
    private String bucket;
}