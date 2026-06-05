package app.budgetmanager.util;

import app.budgetmanager.model.task.AsyncTask;
import app.budgetmanager.model.task.TaskStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AsyncTaskStorage {

    private final Map<String, AsyncTask> tasks = new ConcurrentHashMap<>();

    public AsyncTask create(Long userId) {
        String taskId = UUID.randomUUID().toString();
        AsyncTask asyncTask = new AsyncTask(taskId, userId);
        tasks.put(taskId, asyncTask);
        return tasks.get(taskId);
    }

    public Optional<AsyncTask> get(String taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    public void updateTask(String taskId, TaskStatus status) {
        AsyncTask asyncTask = tasks.get(taskId);
        if (asyncTask == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        asyncTask.setStatus(status);
        tasks.put(taskId, asyncTask);
    }
}
