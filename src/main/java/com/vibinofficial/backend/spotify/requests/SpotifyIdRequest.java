package com.vibinofficial.backend.spotify.requests;

import com.vibinofficial.backend.api.HasuraError;
import com.vibinofficial.backend.spotify.AnyMeta;
import com.vibinofficial.backend.spotify.SpotifyArtist;
import com.vibinofficial.backend.spotify.SpotifyTrack;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.BadRequestException;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class SpotifyIdRequest {

    @NotNull
    public ResponseEntity<?> resolve(SpotifyApi api, String id) {
        try {
            RequestSplit requestSplit = new RequestSplit(id);
            return resolve(api, requestSplit);
        } catch (IllegalArgumentException e) {
            return HasuraError.createResponse(HttpStatus.SC_BAD_REQUEST, new IllegalArgumentException("Invalid id"));
        }
    }

    @NotNull
    private ResponseEntity<?> resolve(SpotifyApi api, RequestSplit request) {
        try {
            AnyMeta obj = createStatement(request.type).run(api, request.id);
            return ResponseEntity.ok(obj);
        } catch (BadRequestException e) {
            return HasuraError.createResponse(404, e);
        } catch (SpotifyWebApiException | ParseException | IOException e) {
            log.error("Exception occurred for resolve request {}: {}", request.id, e);
            return HasuraError.createResponse(500, new IllegalStateException("Unknown Exception occurred"));
        }
    }

    private RequestWrapperStatement createStatement(String type) {
        switch (type) {
            case "track":
                return (api, spotifyId) -> {
                    Track[] tracks = api.getSeveralTracks(spotifyId).build().execute();
                    if (tracks.length > 1) {
                        log.warn("Received multiple tracks ({}) for id {}", tracks.length, spotifyId);
                    }
                    return Arrays.stream(tracks).findFirst().map(SpotifyTrack::new).orElseThrow();
                };

            case "artist":
                return (api, spotifyId) -> {
                    Artist artist = api.getArtist(spotifyId).build().execute();
                    return new SpotifyArtist(artist);
                };
            default:
                throw new IllegalArgumentException();
        }
    }

    private static final class RequestSplit {
        private final String type;
        private final String id;

        RequestSplit(String request) {
            String[] split = request.split(":");
            if (split.length != 3) {
                throw new IllegalArgumentException("Invalid id");
            }

            if (!Objects.equals(split[0], "spotify")) {
                throw new IllegalArgumentException("Invalid id");
            }

            type = split[1];
            id = split[2];
        }
    }

    @FunctionalInterface
    interface RequestWrapperStatement {
        AnyMeta run(SpotifyApi api, String id) throws SpotifyWebApiException, IOException, ParseException;
    }
}
