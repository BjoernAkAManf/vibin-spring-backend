package com.vibinofficial.backend.spotify;

import lombok.Data;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;

@Data
public final class SpotifyTrack implements AnyMeta {
    private final String uri;
    private final String image;
    private final String name;
    private final String[] artists;

    public SpotifyTrack(final Track track) {
        this.uri = track.getUri();
        this.image = Images.getBestImageOf(track);
        this.name = track.getName();
        this.artists = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).toArray(String[]::new);
    }
}
