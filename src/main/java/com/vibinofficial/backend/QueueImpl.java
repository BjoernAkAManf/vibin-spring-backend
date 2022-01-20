package com.vibinofficial.backend;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public final class QueueImpl implements VibinQueue {

    // Random Queue without any smart decisions, first user gets matched with second etc
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void join(final String uid) {
        this.queue.add(uid);
    }

    @Override
    @NonNull
    public Optional<QueueMatch> pollMatch() {
        // Synchronizing and polling every 200 ms results in a noticeable delay (!)
        synchronized (this.queue) {
            if (this.queue.size() < 2) {
                return Optional.empty();
            }

            final String user1 = this.queue.remove();
            final String user2 = this.queue.remove();
            return Optional.of(QueueMatch.of(user1, user2));
        }
    }
}
