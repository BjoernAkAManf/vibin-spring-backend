package com.vibinofficial.backend.spotify;

import lombok.Data;
import se.michaelthelin.spotify.model_objects.specification.Artist;

@Data
public final class SpotifyArtist implements AnyMeta {
    private final String uri;
    private final String image;
    private final String name;

    public SpotifyArtist(final Artist artist) {
        this.uri = artist.getUri();
        this.image = Images.getBestImageOf(artist);
        this.name = artist.getName();
    }

    @Override
    public String[] getArtists() {
        return null;
    }
}
