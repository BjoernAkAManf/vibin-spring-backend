package com.vibinofficial.backend.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class MatchInfo {
    // User you are matched with
    private final String matchUserId;
    // Allows a match to be confirmed or denied
    private final String matchToken;
}
