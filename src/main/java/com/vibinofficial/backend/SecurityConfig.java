package com.vibinofficial.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibinofficial.backend.api.HasuraError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@KeycloakConfiguration
@RequiredArgsConstructor
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
    private final ObjectMapper mapper;
    private final VibinCorsConfig config;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        super.configure(http);

        http.exceptionHandling().accessDeniedHandler(this::accessDeniedHandler);

        http.csrf().disable();

        log.info("CORS Configuration: {}", this.config);
        http.cors().configurationSource(request -> {
            final var cors = new CorsConfiguration();
            cors.setAllowedOrigins(this.config.getOrigins());
            cors.setAllowedMethods(this.config.getMethods());
            cors.setAllowedHeaders(this.config.getHeaders());
            cors.setAllowCredentials(this.config.isCredentials());
            return cors;
        });

        http.authorizeRequests()
                .mvcMatchers("/api/**").authenticated()
                .mvcMatchers("/avatars").authenticated()
                .mvcMatchers("/avatars/**").permitAll()
                .anyRequest().denyAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    @Bean
    @Override
    protected KeycloakAuthenticationProvider keycloakAuthenticationProvider() {
        return super.keycloakAuthenticationProvider();
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    private void accessDeniedHandler(
            final HttpServletRequest request, final HttpServletResponse response, final AccessDeniedException ex
    ) throws IOException {
        HasuraError.writeToHttpResponse(this.mapper, response, HttpServletResponse.SC_FORBIDDEN, ex);
    }
}

