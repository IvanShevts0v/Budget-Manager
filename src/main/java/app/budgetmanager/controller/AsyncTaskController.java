package app.budgetmanager.controller;

import app.budgetmanager.dto.AsyncTaskResponseDto;
import app.budgetmanager.dto.ErrorResponseDto;
import app.budgetmanager.service.AsyncTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
@Tag(name = "Async tasks", description = "Start and track asynchronous background tasks")
@ApiResponse(responseCode = "400", description = "Validation or bad request error",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
@ApiResponse(responseCode = "404", description = "Resource not found",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
@ApiResponse(responseCode = "500", description = "Unexpected server error",
        content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
public class AsyncTaskController {

    private final AsyncTaskService asyncTaskService;

    @PostMapping
    @Operation(summary = "Start async task", description = "Creates and runs a new async task via @Async")
    public AsyncTaskResponseDto startTask(
            @Parameter(description = "User id that owns the task", example = "1")
            @RequestParam Long userId
    ) {
        return asyncTaskService.startTask(userId);
    }

    @PostMapping("/completable-future")
    @Operation(summary = "Start async task via CompletableFuture",
            description = "Creates and runs a new async task using CompletableFuture.runAsync")
    public AsyncTaskResponseDto startTaskWithCompletableFuture(
            @Parameter(description = "User id that owns the task", example = "1")
            @RequestParam Long userId
    ) {
        return asyncTaskService.startTaskWithCompletableFuture(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get async task status", description = "Returns current status of an async task by id")
    public AsyncTaskResponseDto getStatus(
            @Parameter(description = "Task id", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id,
            @Parameter(description = "User id that owns the task", example = "1")
            @RequestParam Long userId
    ) {
        return asyncTaskService.getById(id, userId);
    }
}
