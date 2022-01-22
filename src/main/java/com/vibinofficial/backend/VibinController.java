package com.vibinofficial.backend;

import com.vibinofficial.backend.api.*;
import com.vibinofficial.backend.twilio.VibinConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
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
@EnableAsync
public class VibinController {

    private final VibinQueue queueService;
    private final VibinConfig config;
    private final Map<String, List<Consumer<String>>> events = new HashMap<>();

    @Scheduled(fixedRate = 1000, timeUnit = TimeUnit.MILLISECONDS)
    public void createPolling() {
        if (!this.config.isEnabled()) {
            log.warn("Polling disabled!");
            return;
        }
        final Optional<QueueMatch> queueMatch = this.queueService.pollMatch();
        queueMatch.ifPresent(this::dispatchMatchEvent);
    }

    private void dispatchMatchEvent(QueueMatch queueMatch) {
        final String user1 = queueMatch.getUser1();
        final String user2 = queueMatch.getUser2();

        log.info("Dispatching Match {} with {}", user1, user2);
        dispatchMatchEvent(user1, user2);
        dispatchMatchEvent(user2, user1);
    }

    private void dispatchMatchEvent(final String user, final String match) {
        Optional.ofNullable(this.events.get(user))
                .flatMap(i -> i.stream().findFirst())
                .ifPresentOrElse(
                        (handler) -> {
                            log.warn("Handlers: {}", this.events);
                            log.warn("Handlers: {}", handler);
                            handler.accept(match);
                        },
                        () -> log.warn("User Event scheduled, but has not yet joined: {}", user)
                );
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<HasuraError> onTimeoutException(final AsyncRequestTimeoutException ex) {
        log.info("some timeout occurred {}", ex);
        // TODO remove on timeout, timestamp in queue?
        return HasuraError.createResponse(HttpStatus.SC_REQUEST_TIMEOUT, ex);
    }

    @Async
    @PostMapping("/queue/join")
    @AsyncAction
    public Mono<MatchInfo> joinQueue(final Principal principal) {
        final String user = principal.getName();
        log.info("joining queue POST: {}", user);
        // Add to Queue
        this.queueService.join(user);

        // TODO: Check Criteria list (e.g. Language; Interest, if applicable)
        // TODO: Check Blocklist
        // TODO: Check TMP Blocklist (Mismatches do not get rematched until a cool off period)

        // wait until we have a partner
        log.info("Creating sink");
        Mono<MatchInfo> sinkerino = Mono.create(sink -> {
            final List<Consumer<String>> userEvents = this.events.computeIfAbsent(user, k -> new ArrayList<>());
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
        log.info("Finished creating sink");

//        return sinkerino.doOnError(x -> log.info("FOOOO " + x.toString()));
//
//        Mono.defer()
//            Mono.just()
//        sinkerino.delaySubscription()
        return sinkerino;
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
        final String uid = principal.getName();
        // Server notifies waiting clients in syncRoom.
    }

    // TODO: If any party does not join a room within x minutes after a successful match, complete room automatically
    // TODO: If any party leaves a room permanently (in contrast to closing the window) complete room automatically
}
