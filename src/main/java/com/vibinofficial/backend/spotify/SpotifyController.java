package com.vibinofficial.backend.spotify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping(path = "/spotify")
@RequiredArgsConstructor
public class SpotifyController {
    private final SpotifyConfig config;
    private LocalDateTime nextCheck;

    @Scheduled(initialDelay = 0, fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void updateToken() throws IOException, ParseException, SpotifyWebApiException {
        final var now = LocalDateTime.now();

        if (nextCheck != null && nextCheck.isAfter(now)) {
            return;
        }

        final var api = this.config.getApi();
        final var resp = api.clientCredentials().build().execute();
        api.setAccessToken(resp.getAccessToken());
        final var nextCheckIn = resp.getExpiresIn() / 2;
        this.nextCheck = now.plus(nextCheckIn, ChronoUnit.SECONDS);
        log.info("Updated Access Token, next check in {}s", nextCheckIn);
    }

    @GetMapping("/artists")
    public SpotifyArtist[] artists(
            @RequestParam("q") final String query,
            @RequestParam(value = "offset", defaultValue = "0") final int offset,
            @RequestParam(value = "limit", defaultValue = "20") final int limit
    ) throws IOException, ParseException, SpotifyWebApiException {
        final var api = this.config.getApi();
        final var l = api.searchArtists(query)
                .offset(offset)
                .limit(Math.min(limit, this.config.getSearchLimit()))
                .build()
                .execute();

        return Arrays.stream(l.getItems())
                .map(SpotifyArtist::new)
                .toArray(SpotifyArtist[]::new);
    }

    @GetMapping("/tracks")
    public SpotifyTrack[] tracks(
            @RequestParam("q") final String query,
            @RequestParam(value = "offset", defaultValue = "0") final int offset,
            @RequestParam(value = "limit", defaultValue = "20") final int limit
    ) throws IOException, ParseException, SpotifyWebApiException {
        final var api = this.config.getApi();
        final var l = api.searchTracks(query)
                .offset(offset)
                .limit(Math.min(limit, this.config.getSearchLimit()))
                .build()
                .execute();

        return Arrays.stream(l.getItems())
                .map(SpotifyTrack::new)
                .toArray(SpotifyTrack[]::new);
    }
}
