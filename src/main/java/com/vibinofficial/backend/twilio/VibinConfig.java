package com.vibinofficial.backend.twilio;

import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VideoGrant;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("vibin")
public class VibinConfig {

    private boolean disabled;
    private String token;
    private String key;
    private String account;

    @NotNull
    AccessToken.Builder getGrantBuilder(String roomSid) {
        return new AccessToken.Builder(getAccount(), getKey(), getToken()).grant(new VideoGrant().setRoom(roomSid));
    }
}
