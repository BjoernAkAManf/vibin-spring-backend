package com.vibinofficial.backend.avatars;

import com.vibinofficial.backend.util.KeycloakExtendedClient;
import com.vibinofficial.backend.util.KeycloakUser;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@RequiredArgsConstructor
class AvatarsTest {
    @Container
    private static final MinioContainer MINIO = new MinioContainer();
    @Container
    private static final KeycloakContainer KEYCLOAK = new KeycloakContainer()
        .withRealmImportFile("realm.json");

    @Inject
    private MockMvc mvc;

    private static KeycloakUser user;

    @DynamicPropertySource
    static void createConfig(final DynamicPropertyRegistry registry) {
        System.out.println("YYYYYYYY | " + MINIO);
        registry.add("services.minio.host", MINIO::getUrl);
        registry.add("services.minio.accessKey", MINIO::getUsername);
        registry.add("services.minio.secretKey", MINIO::getPassword);
        registry.add("services.minio.bucket", () -> "meow");

        registry.add("vibin.disabled", () -> true);

        registry.add("keycloak.realm", () -> "aabbcc");
        registry.add("keycloak.auth-server-url", KEYCLOAK::getAuthServerUrl);
        registry.add("keycloak.ssl-required", () -> "none");
        registry.add("keycloak.resource", () -> "aabbcc-client");
        registry.add("keycloak.public-client", () -> true);
        registry.add("keycloak.confidential-port", () -> 0);
    }

    @BeforeAll
    static void createUser() {
        final var client = KEYCLOAK.getKeycloakAdminClient();
        final var clientEx = new KeycloakExtendedClient(client, KEYCLOAK.getAuthServerUrl());
        user = clientEx.createUser("aabbcc", "aaa", "bbb");

        createBucket();
    }

    @Test
    void reading() {
        final var token = login();
        final var client = MINIO.getClient();

        final var uuid = UUID.randomUUID();
        Assertions.assertDoesNotThrow(() -> {
            final var meow = new StringBuilder()
                .append("My Test Content: ");

            for (var i = 0; i < 1000; i += 1) {
                meow.append('\n')
                    .append("- #").append(i);
            }
            final var s = new ByteArrayInputStream(meow.toString().getBytes(StandardCharsets.UTF_8));

            return client
                    .putObject(PutObjectArgs.builder()
                        .bucket("meow")
                        .object(uuid + ".jpg")
                        .headers(Map.of(
                            // Not setting the header causes an error
                            // TODO: Not setting this should throw an application error not return whatever is stored (e.g. Whitelist)
                            // TODO: Test upload not possible if not authenticated
                            HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE
                        ))
                        .stream(s, -1, 5 * 1024 * 1024)
                        .build());
            }
        );

        // Login Works with Authorization
        Assertions.assertDoesNotThrow(() -> this.mvc
            .perform(MockMvcRequestBuilders
                .get("/avatars/{uuid}", uuid)
                .header(HttpHeaders.AUTHORIZATION, token))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG))
        );

        // Login Works without Authorization
        Assertions.assertDoesNotThrow(() -> this.mvc
            .perform(MockMvcRequestBuilders
                .get("/avatars/{uuid}", uuid))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG))
        );
    }

    @Test
    void writing() throws IOException {
        final var client = MINIO.getClient();
        final var token = login();

        // TODO: Support Cat.avif as input / output
        final var input = Objects
            .requireNonNull(
                getClass().getResourceAsStream("/cat.png"),
                "Loading Cat = Failed"
            )
            .readAllBytes();

        Assertions.assertDoesNotThrow(() -> this.mvc
            .perform(MockMvcRequestBuilders.post("/avatars")
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.AUTHORIZATION, token)
                .content(input))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json("{\"success\": true}", true))
        );

        // TODO: Add Test: PNG is uploaded as istest
        @Cleanup final var is = Assertions.assertDoesNotThrow(() -> client
            .getObject(GetObjectArgs.builder()
                .bucket("meow")
                .object(user.getId() + ".jpg")
                .build())
        );
        final var actual = is.readAllBytes();
        final var expected = this.convert(new ByteArrayInputStream((input)));

        Assertions.assertArrayEquals(expected, actual);
    }

    private byte[] convert(final InputStream is) throws IOException {
        final var image = ImageIO.read(is);

        Assertions.assertNotNull(image, "Image should not be null!");
        final var img = new BufferedImage(
            image.getWidth(),
            image.getHeight(),
            BufferedImage.TYPE_INT_BGR
        );

        img.createGraphics()
            .drawImage(image, 0, 0, Color.white, null);

        final var bos = new ByteArrayOutputStream();
        Assertions.assertTrue(
            ImageIO.write(img, "jpeg", bos),
            "Encoding failed"
        );
        return bos.toByteArray();
    }

    private static void createBucket() {
        final var client = MINIO.getClient();
        Assertions.assertDoesNotThrow(() -> {
            client
                .makeBucket(MakeBucketArgs.builder()
                    .bucket("meow")
                    .build()
                );
        });
    }

    private String login() {
        return Assertions.assertDoesNotThrow(() -> user.login("aabbcc-client").getBearerToken());
    }
}
