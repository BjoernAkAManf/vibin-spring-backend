package com.vibinofficial.backend.avatars.minio;

import com.vibinofficial.backend.avatars.AvatarStorage;
import io.minio.GetObjectResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
final class MinioAvatar implements AvatarStorage.Avatar {
    private final GetObjectResponse response;

    @Override
    public InputStream open() {
        return this.response;
    }

    @Override
    public String getMediaType() {
        return this.response.headers().get("Content-Type");
    }

    @Override
    public void close() throws IOException {
        this.response.close();
    }
}
