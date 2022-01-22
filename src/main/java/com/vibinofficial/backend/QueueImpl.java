package com.vibinofficial.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
public final class QueueImpl implements VibinQueue {
    private final Object readLock = new Object();

    // Random Queue without any smart decisions, first user gets matched with second etc
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void join(final String uid) {
        log.info("Begin join");
        if (this.queue.contains(uid)) {
            log.info("User already in queue: {}", uid);
        } else {
            log.info("User joined the queue: {}", uid);
            this.queue.add(uid);
        }
        log.info("End join");
    }

    @Override
    @NonNull
    public Optional<QueueMatch> pollMatch() {
        // Synchronizing and polling every 200 ms results in a noticeable delay (?)
        synchronized (this.queue) {
            final int size = this.queue.size();
            List<String> shortenedQueueEntries = this.queue.stream().map(x -> x.substring(0, 4)).collect(Collectors.toList());
            log.info("Queue size: {}: {}", size, shortenedQueueEntries);
            if (size < 2) {
                return Optional.empty();
            }

            final String user1 = this.queue.remove();
            final String user2 = this.queue.remove();
            final var s = this.queue.size();
            log.info("Matching users: {}, {} ({})", user1, user2, s);
            return Optional.of(QueueMatch.of(user1, user2));
        }
    }
}
