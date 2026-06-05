package app.budgetmanager.dto;

import app.budgetmanager.model.task.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Async task status response")
public class AsyncTaskResponseDto {

    @Schema(description = "Unique task identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String taskId;

    @Schema(description = "Owner user id", example = "1")
    private Long userId;

    @Schema(description = "Current task status", example = "IN_PROGRESS")
    private TaskStatus taskStatus;
}
