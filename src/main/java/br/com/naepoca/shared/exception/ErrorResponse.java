package br.com.naepoca.shared.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(Instant timestamp, String code, String message, List<String> details) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(Instant.now(), code, message, List.of());
    }
    public static ErrorResponse of(String code, String message, List<String> details) {
        return new ErrorResponse(Instant.now(), code, message, details);
    }
}
