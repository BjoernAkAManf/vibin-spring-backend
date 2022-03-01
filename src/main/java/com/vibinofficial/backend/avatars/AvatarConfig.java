package com.vibinofficial.backend.avatars;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.inject.Named;
import java.util.concurrent.Executors;

@Configuration
public class AvatarConfig {
    @Bean
    @Primary
    public AvatarStorage storage(final @Named("avatar-storage-minio") AvatarStorage storage) {
        return new JPGConverter(storage, Executors.newCachedThreadPool());
    }
}
