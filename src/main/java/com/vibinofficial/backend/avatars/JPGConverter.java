package com.vibinofficial.backend.avatars;

import lombok.RequiredArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class JPGConverter implements AvatarStorage {
    private final AvatarStorage storage;
    private final ExecutorService tasks;

    @Override
    public Avatar read(final String path) throws IOException {
        String jpegPath = this.translatePath(path);
        return this.storage.read(jpegPath);
    }

    @Override
    public void write(final String path, final InputStream is) throws IOException {
        final var image = ImageIO.read(is);
        if (image == null) {
            throw new UnsupportedOperationException("Reading Image failed");
        }

        final var img = new BufferedImage(
            image.getWidth(),
            image.getHeight(),
            BufferedImage.TYPE_INT_BGR
        );

        img.createGraphics()
            // TODO: Might be nicer to have custom bg color
            .drawImage(image, 0, 0, Color.white, null);

        final var in = new PipedInputStream();
        final var out = new PipedOutputStream(in);

        final var f1 = this.tasks.submit(() -> {
            this.storage.write(this.translatePath(path), in);
            return null;
        });

        final var f2 = this.tasks.submit(() -> {
            // Note: Closing is important, otherwise our input stream will block endlessly
            try(final var ignored = out) {
                if (!ImageIO.write(img, "jpeg", out)) {
                    throw new UnsupportedOperationException("Could not re-encode Image");
                }
                return null;
            }
        });

        try {
            // TODO: Timeout may not be enough
            f1.get(5, TimeUnit.SECONDS);
            f2.get(5, TimeUnit.SECONDS);
        } catch (final InterruptedException | TimeoutException ex) {
            f1.cancel(true);
            f2.cancel(true);
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            final var cause = ex.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException("Execution error", cause);
        }
    }

    private String translatePath(final String path) {
        return path + ".jpg";
    }
}
