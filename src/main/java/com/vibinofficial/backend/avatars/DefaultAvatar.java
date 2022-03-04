package com.vibinofficial.backend.avatars;

import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class DefaultAvatar implements AvatarStorage.Avatar {
    private static final URL AVATAR = DefaultAvatar.class.getResource("/default_avatar.png");

    @Override
    public InputStream open() throws IOException {
        if (AVATAR == null) {
            throw new IOException("Default Avatar not found!");
        }
        return AVATAR.openStream();
    }

    @Override
    public String getMediaType() {
        return MediaType.IMAGE_PNG_VALUE;
    }

    @Override
    public void close() {

    }
}
