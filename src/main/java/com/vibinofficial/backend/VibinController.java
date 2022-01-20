package com.vibinofficial.backend;

import com.vibinofficial.backend.api.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.javatuples.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class VibinController {
    private final QueueImpl queue;
    private final Map<String, List<Consumer<String>>> events = new HashMap<>();

    @Scheduled(fixedRate = 200, timeUnit = TimeUnit.MILLISECONDS)
    public void createPolling() {
        final Optional<Pair<String, String>> poll = this.queue.poll();
        poll.ifPresent(this::dispatchMatchEvent);
    }

    private void dispatchMatchEvent(Pair<String, String> poll) {
        final var u1 = poll.getValue1();
        final var u2 = poll.getValue0();
        log.info("Dispatching Match {} with {}", u1, u2);
        dispatchEvent(u1, u2);
        dispatchEvent(u2, u1);
    }

    private void dispatchEvent(final String user, final String match) {
        final var handler = Optional.ofNullable(this.events.get(user))
                .flatMap(i -> i.stream().findFirst())
                .orElse(null);
        if (handler == null) {
            log.warn("User Event scheduled, but has not yet joined: {}", user);
            return;
        }
        handler.accept(match);
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<HasuraError> onTimeoutException(final AsyncRequestTimeoutException ex) {
        return HasuraError.createResponse(HttpStatus.SC_REQUEST_TIMEOUT, ex);
    }

    @PostMapping("/queue/join")
    @AsyncAction
    public Mono<MatchInfo> joinQueue(final Principal principal) {
        final var user = principal.getName();
        // Add to Queue
        this.queue.join(user);

        // TODO: Check Criteria list (e.g. Language; Interest, if applicable)
        // TODO: Check Blocklist
        // TODO: Check TMP Blocklist (Mismatches do not get rematched until a cool off period)

        // wait until we has partner
        return Mono.create(sink -> {
            // (╯°□°）╯︵ ┻━┻
            final var userEvents = this.events.computeIfAbsent(user, k -> new ArrayList<>());
            final Consumer<String> onMatchFound = match -> sink.success(
                    MatchInfo.builder()
                            .matchUserId(match)
                            .matchToken("my-token-" + user)
                            .build()
            );

            // Can be called externally: Return Match
            userEvents.add(onMatchFound);
            sink.onDispose(() -> userEvents.remove(onMatchFound));
        });
    }

    @AsyncAction
    public RoomInfo syncMatch(final Principal principal) {
        // IF you already accepted
        //    await Partner response
        //    accepted: create room (for both match participants)
        //    declined: return cancelled (for both match participants)

        // IF you already declined
        //    return cancelled (for both match participants)

        // If no decision and your partner decides
        //      declined: return cancelled (for both match)
        //      accepted: await your decision

        // Otherwise: await any response
        // TODO: Also at most wait x minutes otherwise decline automatically
        return RoomInfo.builder()
                .success(false)
                .token(principal.getName())
                .build();
    }

    @SyncAction
    public void respondToMatch(final Principal principal, final String token, final boolean accept) {
        final var uid = principal.getName();
        // Server notifies waiting clients in syncRoom.
    }

    // TODO: If any party does not join a room within x minutes after a successful match, complete room automatically
    // TODO: If any party leaves a room permanently (in contrast to closing the window) complete room automatically
}
