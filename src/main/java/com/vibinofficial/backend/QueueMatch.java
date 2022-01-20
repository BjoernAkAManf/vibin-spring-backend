package com.vibinofficial.backend;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QueueMatch {

    private final String user1;
    private final String user2;

    public static QueueMatch of(String user1, String user2) {
        return new QueueMatch(user1, user2);
    }
}
