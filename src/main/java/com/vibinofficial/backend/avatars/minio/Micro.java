package com.vibinofficial.backend.avatars.minio;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Singleton;

@Configuration
public class Micro {
    @Bean
    @Singleton
    public MinioClient createClient(final Config config) {
        System.out.println("XXXXX | " +config);
        return MinioClient.builder()
            .endpoint(config.getHost())
            .credentials(config.getAccessKey(), config.getSecretKey())
            .build();
    }
}
