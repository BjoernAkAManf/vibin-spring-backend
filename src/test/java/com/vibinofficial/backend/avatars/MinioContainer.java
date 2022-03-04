package com.vibinofficial.backend.avatars;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public final class MinioContainer extends GenericContainer<MinioContainer> {
    private static final int PORT = 9000;
    private static final String DEFAULT_USER = "def-user-123456";
    private static final String DEFAULT_PASSWORD = "def-pw-123456";
    @Getter
    private String password;

    @Getter
    private String username;

    public MinioContainer() {
        super(DockerImageName.parse("quay.io/minio/minio:latest"));
        this.withExposedPorts(PORT);

        this.withCommand("server", "/data");
        this.withRootUser(DEFAULT_USER);
        this.withRootPassword(DEFAULT_PASSWORD);
    }

    public MinioContainer withRootUser(final String user) {
        this.username = user;
        return this.withEnv("MINIO_ROOT_USER", user);
    }

    public MinioContainer withRootPassword(final String password) {
        this.password = password;
        return this.withEnv("MINIO_ROOT_PASSWORD", password);
    }

    public String getUrl() {
        return String.format("http://%s:%d", this.getContainerIpAddress(), this.getMappedPort(PORT));
    }

    public MinioClient getClient() {
        return MinioClient.builder()
            .endpoint(this.getUrl())
            .credentials(this.username, this.password)
            .build();
    }
}
