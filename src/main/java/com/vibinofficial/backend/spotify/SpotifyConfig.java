package com.vibinofficial.backend.spotify;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import se.michaelthelin.spotify.SpotifyApi;

@Data
@Configuration
@ConfigurationProperties("services.spotify")
public class SpotifyConfig {
    private boolean disabled;
    private String clientId;
    private String clientSecret;
    private int searchLimit = 20;

    @Getter(lazy = true)
    private final SpotifyApi api = createApi();

    SpotifyApi createApi() {
        return new SpotifyApi.Builder()
                .setClientId(this.clientId)
                .setClientSecret(this.clientSecret)
                .build();
    }
}
