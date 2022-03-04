package com.vibinofficial.backend;

import com.vibinofficial.backend.avatars.AvatarStorage;
import com.vibinofficial.backend.avatars.minio.MinioExceptions;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.ConnectException;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/avatars")
@RequiredArgsConstructor
public class Avatars {
    private final AvatarStorage storage;

    @PostMapping
    // TODO: Remove
    @CrossOrigin("http://localhost:8080")
    public Map<String, Object> upload(final Principal user, final HttpServletRequest req) throws IOException {
        final var name = user.getName();
        this.storage.write(name, req.getInputStream());
        return Map.of("success", true);
    }

    @GetMapping("/{user}")
    public ResponseEntity<?> index(@PathVariable("user") final UUID user) throws IOException {
        try (final var m = this.storage.read(user.toString())) {
            return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(m.getMediaType()))
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                // TODO: May be less great to read all in memory
                .body(m.open().readAllBytes());
        } catch (final ConnectException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("BAD GATEWAY");
        } catch (final IOException ex) {
            final var cause = ex.getCause();
            if (cause instanceof ErrorResponseException) {
                return MinioExceptions.wrap((ErrorResponseException) cause);
            }
            throw ex;
        }
    }
}
