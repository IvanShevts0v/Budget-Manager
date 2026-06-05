package app.budgetmanager.service;

import app.budgetmanager.dto.AsyncTaskResponseDto;
import app.budgetmanager.mapper.AsyncTaskMapper;
import app.budgetmanager.model.task.AsyncTask;
import app.budgetmanager.repository.UserRepository;
import app.budgetmanager.util.AsyncTaskRunner;
import app.budgetmanager.util.AsyncTaskStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskService {

    private final AsyncTaskStorage asyncTaskStorage;
    private final AsyncTaskMapper asyncTaskMapper;
    private final AsyncTaskRunner asyncTaskRunner;
    private final UserRepository userRepository;

    public AsyncTaskResponseDto startTask(Long userId) {
        validateUser(userId);
        AsyncTask task = asyncTaskStorage.create(userId);
        asyncTaskRunner.executeTask(task.getId());
        return asyncTaskMapper.toDto(task);
    }

    public AsyncTaskResponseDto startTaskWithCompletableFuture(Long userId) {
        validateUser(userId);
        AsyncTask task = asyncTaskStorage.create(userId);
        asyncTaskRunner.executeTaskFuture(task.getId())
                .exceptionally(ex -> {
                    log.error("CompletableFuture task {} failed", task.getId(), ex);
                    return null;
                });
        return asyncTaskMapper.toDto(task);
    }

    public AsyncTaskResponseDto getById(String taskId, Long userId) {
        validateUser(userId);
        AsyncTask task = asyncTaskStorage.get(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!task.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return asyncTaskMapper.toDto(task);
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
