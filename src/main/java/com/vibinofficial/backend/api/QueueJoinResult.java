package com.vibinofficial.backend.api;

import lombok.Data;

@Data
public final class QueueJoinResult {
    public static final QueueJoinResult SUCCESS = new QueueJoinResult(true);
    public static final QueueJoinResult ERROR = new QueueJoinResult(false);
    private final boolean success;
}
