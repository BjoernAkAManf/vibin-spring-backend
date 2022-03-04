package com.vibinofficial.backend.util;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Credentials {
    @JsonProperty("access_token")
    private String accessToken;

    public String getBearerToken() {
        return "Bearer " + this.accessToken;
    }
}
