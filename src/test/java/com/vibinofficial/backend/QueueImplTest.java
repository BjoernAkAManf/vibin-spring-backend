package com.vibinofficial.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class QueueImplTest {
    @Inject
    private QueueImpl queue;

    @Test
    @Timeout(value = 1)
    public void single() {
        this.runSimulation("abc", "def");
        assertThat(queue.getQueue()).isEmpty();
    }

    @Test
    @Timeout(value = 1)
    public void multi() {
        assertThat(queue.getQueue()).isEmpty();
        var threads = new ArrayList<Thread>();
        var errors = new ArrayList<Throwable>();
        var n = 1000;

        for (int i = 0; i < n; i += 1) {
            int prefix = i;
            final var thread = this.createThread(() -> this.join("a" + prefix, "b" + prefix, errors), errors);
            threads.add(thread);
            thread.start();
        }

        threads.forEach(t -> assertThatCode(t::join).doesNotThrowAnyException());
        assertThat(queue.getQueue()).hasSize(n * 2);
        assertThat(errors).isEmpty();

        for (int i = 0; i < n; i += 1) {
            final var opt = this.queue.pollMatch();
            assertThat(opt).as("Match not found! %d", i).isPresent();
        }

        assertThat(queue.getQueue()).isEmpty();
    }

    private void runSimulation(final String u1, final String u2) {
        final var errors = new ArrayList<Throwable>();
        this.join(u1, u2, errors);

        final var opt = this.queue.pollMatch();
        assertThat(opt).as("Match not found!").isPresent();
        final var match = opt.get();
        assertThat(List.of(match.getUser1(), match.getUser2())).containsExactlyInAnyOrder(u1, u2);
        assertThat(errors).isEmpty();
    }

    private void join(final String u1, final String u2, final List<Throwable> errors) {
        assertThatCode(() -> {
            final var t1 = this.createThread(() -> this.queue.join(u1), errors);
            final var t2 = this.createThread(() -> this.queue.join(u2), errors);

            t1.start();
            t2.start();

            t1.join();
            t2.join();
        })
                .as("Could not start Join Request Threads")
                .doesNotThrowAnyException();
    }

    private Thread createThread(final Runnable r, final List<Throwable> errors) {
        Thread thread = new Thread(r);
        thread.setUncaughtExceptionHandler((thread1, throwable) -> errors.add(throwable));
        return thread;

    }
}
