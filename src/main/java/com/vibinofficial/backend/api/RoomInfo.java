package com.vibinofficial.backend.api;

import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@Builder
public final class RoomInfo {
    private final boolean success;

    @Nullable // Only set if success is true, personalized to a given user-id
    private final String roomName;

    @Nullable // Only set if success is true, personalized to a given user-id
    private final String token;
}
