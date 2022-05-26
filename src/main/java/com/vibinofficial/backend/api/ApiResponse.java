package com.vibinofficial.backend.api;

import lombok.Data;

@Data
public final class ApiResponse {
    public static final ApiResponse SUCCESS = new ApiResponse(true);
    public static final ApiResponse ERROR = new ApiResponse(false);
    private final boolean success;
}
