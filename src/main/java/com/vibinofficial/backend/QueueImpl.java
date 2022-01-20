package com.vibinofficial.backend;

import org.javatuples.Pair;
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
    public Optional<Pair<String, String>> poll() {
        // Synchronizing and polling every 200 ms results in a noticeable delay (!)
        synchronized (this.queue) {
            if (this.queue.size() < 2) {
                return Optional.empty();
            }
            return Optional.of(Pair.with(this.queue.remove(), this.queue.remove()));
        }
    }
}
