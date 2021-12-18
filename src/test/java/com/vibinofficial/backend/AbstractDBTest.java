package com.vibinofficial.backend;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

@SpringBootTest
@Testcontainers
abstract class AbstractDBTest {
    private static final String ROOT_PW = "root-pw";
    private static final String DATABASE = "db";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pass";
    private static final int PORT = 3306;

    @Container
    public static final GenericContainer<?> DB = new GenericContainer<>("mariadb:10.7.1")
            .withExposedPorts(PORT)
            .withEnv("MYSQL_ROOT_PASSWORD", ROOT_PW)
            .withEnv("MYSQL_DATABASE", DATABASE)
            .withEnv("MYSQL_USER", USERNAME)
            .withEnv("MYSQL_PASSWORD", PASSWORD)
            .withReuse(true);

    @Configuration
    public static class DataSourceConfig {
        @Bean
        @Primary
        public DataSource dataSource() {
            final var url = String.format("jdbc:mariadb://%s:%d/%s", DB.getContainerIpAddress(), DB.getMappedPort(PORT), DATABASE);
            return DataSourceBuilder
                    .create()
                    .url(url)
                    .username(USERNAME)
                    .password(PASSWORD)
                    .build();
        }
    }
}
