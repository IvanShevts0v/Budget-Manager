package app.budgetmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

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

    public ErrorResponseDto(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
