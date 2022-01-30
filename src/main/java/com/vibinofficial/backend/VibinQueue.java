package com.vibinofficial.backend;

import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * Implements logic with regard to joining and leaving voice calls.
 */
public interface VibinQueue {

    /**
     * Called whenever a given User wants to join the queue.
     *
     * @param uid User ID
     */
    void join(String uid);

    /**
     * Called whenever a given User wants to leave the queue.
     *
     * @param uid User ID
     */
    void leave(String uid);

    /**
     * Called whenever a given User no longer wants to join a call.
     *
     * @param uid User ID
     */
    void remove(String uid);

    @NonNull
    Optional<QueueMatch> pollMatch();
}
