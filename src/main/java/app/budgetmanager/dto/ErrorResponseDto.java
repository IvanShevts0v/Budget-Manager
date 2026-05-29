package app.budgetmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
@Schema(description = "Unified API error response")
public class ErrorResponseDto {

    @Schema(description = "Error timestamp", example = "2026-05-27T12:00:00")
    private final LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private final int status;

    @Schema(description = "HTTP status reason phrase", example = "Bad Request")
    private final String error;

    @Schema(description = "Error message", example = "Validation failed")
    private final String message;

    @Schema(description = "Request path", example = "/users/register")
    private final String path;

    @Schema(description = "Field-level validation errors")
    private final Map<String, String> validationErrors;
}
