package com.vibinofficial.backend.avatars.minio;

import com.vibinofficial.backend.avatars.AvatarStorage;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Service
@Named("avatar-storage-minio")
@RequiredArgsConstructor
public final class Storage implements AvatarStorage {
    private final MinioClient client;
    private final Config config;

    @Override
    public Avatar read(final String path) throws IOException {
        final var result = this.create(this.config.getBucket(), path);
        if (result == null) {
            return AvatarStorage.DEFAULT;
        }
        return new MinioAvatar(result);
    }

    @Override
    public void write(final String path, final InputStream is, final String mediaType) throws IOException {
        try {
            this.client.putObject(PutObjectArgs.builder()
                .bucket(this.config.getBucket())
                .object(path)
                .headers(Map.of("Content-Type", mediaType))
                .stream(is, -1, 5 * 1024 * 1024)
                .build());
        } catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException ex) {
            throw new IOException("Error uploading minio", ex);
        }
    }

    private GetObjectResponse create(final String bucket, final String obj) throws IOException {
        try {
            return this.client.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(obj)
                    .build());
        } catch (final ErrorResponseException ex) {
            final var code = ex.errorResponse().code();
            if ("NoSuchKey".equals(code)) {
                return null;
            }
            throw new IOException("Error accessing minio", ex);
        } catch (final MinioException | InvalidKeyException | NoSuchAlgorithmException ex) {
            throw new IOException("Error accessing minio", ex);
        }
    }
}
