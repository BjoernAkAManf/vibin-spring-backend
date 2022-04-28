package com.vibinofficial.backend.spotify;

import lombok.experimental.UtilityClass;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

@UtilityClass
public final class Images {
    private static final Comparator<Image> BIG_BERTA_IMAGE = Comparator.comparing(i -> i.getHeight() * i.getWidth());

    public static String getBestImageOf(Track track) {
        Stream<Image> images = Optional.of(track).map(Track::getAlbum).map(AlbumSimplified::getImages).stream().flatMap(Arrays::stream);
        return Images.getBestImageOf(images);
    }

    public static String getBestImageOf(Artist artist) {
        return getBestImageOf(Arrays.stream(artist.getImages()));
    }

    public static String getBestImageOf(final Stream<Image> images) {
        return images.max(BIG_BERTA_IMAGE).map(Image::getUrl).orElse(null);
    }
}
