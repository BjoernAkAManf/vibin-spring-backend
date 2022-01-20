package com.vibinofficial.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HasuraError {
    private final String code;
    private final String message;
    private final Object extensions = new Object[0];

    public HasuraError(final int status, final Exception ex) {
        this(String.valueOf(status), Optional.of(ex).map(Exception::getMessage).orElseGet(() -> ex.getClass().getSimpleName()));
    }

    public static ResponseEntity<HasuraError> createResponse(final int status, final Exception ex) {
        return ResponseEntity.status(status).body(new HasuraError(status, ex));
    }

    public static void writeToHttpResponse(
            final ObjectMapper mapper, final HttpServletResponse response, final int code, final Exception ex
    ) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);
        mapper.writeValue(response.getWriter(), new HasuraError(code, ex));
    }
}
