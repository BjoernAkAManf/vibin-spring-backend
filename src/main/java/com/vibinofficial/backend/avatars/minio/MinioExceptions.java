package com.vibinofficial.backend.avatars.minio;

import io.minio.errors.ErrorResponseException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
@UtilityClass
public final class MinioExceptions {
    public ResponseEntity<String> wrap(final ErrorResponseException ex) {
        final var code = ex.errorResponse().code();
        switch (code) {
            case "NoSuchBucket":
                return ResponseEntity.internalServerError().body("Issue with Storage #1");
            case "NoSuchKey":
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
            default:
                log.error("Unknown Error: Code {}", code, ex);
                return ResponseEntity.internalServerError().body(null);
        }
    }
}
