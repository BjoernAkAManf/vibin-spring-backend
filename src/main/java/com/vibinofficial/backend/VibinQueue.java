package com.vibinofficial.backend;

import org.javatuples.Pair;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * Implements logic with regard to joining and leaving voice calls.
 */
public interface VibinQueue {
    /**
     * Called whenever a given User wants to join a call.
     *
     * @param uid User ID
     */
    void join(String uid);

    @NonNull
    Optional<Pair<String, String>> poll();
}
