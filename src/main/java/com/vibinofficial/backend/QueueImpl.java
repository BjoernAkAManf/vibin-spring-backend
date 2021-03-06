package com.vibinofficial.backend;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@Slf4j
@Service
public final class QueueImpl implements VibinQueue {

    // Random Queue without any smart decisions, first user gets matched with second etc
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void join(final String uid) {
        if (this.queue.contains(uid)) {
            log.info("User already in queue: {}", uid);
        } else {
            log.info("User joined the queue: {} ({})", uid, this.queue.size());
            this.queue.add(uid);
        }
    }

    private void logQueue() {
        List<String> shortenedQueueEntries = UidUtils.shorten(this.queue);
        log.info("Queue size: {}: {}", this.queue.size(), shortenedQueueEntries);
    }

    @Override
    public void leave(String uid) {
        if (!this.queue.contains(uid)) {
            log.info("User was not in queue: {}", uid);
        } else {
            log.info("User left the queue: {} ({})", uid, this.queue.size());
            this.queue.remove(uid);
        }
    }

    @Override
    public void remove(final String uid) {
        this.queue.remove(uid);
    }

    @Override
    @NonNull
    public Optional<QueueMatch> pollMatch() {
        synchronized (this.queue) {
            final int size = this.queue.size();

            if (size >= 2) {
                QueueMatch queueMatch = createQueueMatch();
                return Optional.of(queueMatch);
            } else {
                return Optional.empty();
            }

        }
    }

    @NonNull
    private QueueMatch createQueueMatch() {
        final String user1 = this.queue.remove();
        final String user2 = this.queue.remove();
        final int s = this.queue.size();

        log.info("Matched users: {}, {} ({} users remaining)", UidUtils.shorten(user1), UidUtils.shorten(user2), s);
        return new QueueMatch(user1, user2);
    }
}
