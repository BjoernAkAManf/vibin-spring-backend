package com.vibinofficial.backend;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class QueueMatch {
    private final String user1;
    private final String user2;

    public String getLexSmallerUser() {
        return user1.compareTo(user2) < 0 ? user1 : user2;
    }

    public String getLexGreaterUser() {
        return user1.compareTo(user2) > 0 ? user1 : user2;
    }
}
