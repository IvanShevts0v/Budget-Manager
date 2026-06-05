package app.budgetmanager.util;

import app.budgetmanager.model.task.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class AsyncTaskExecutor implements AsyncTaskRunner {

    public static final int TASK_EXECUTION_TIME_MILLIS = 10_000;

    private final AsyncTaskStorage asyncTaskStorage;
    private final Executor taskExecutor;

    public AsyncTaskExecutor(
            AsyncTaskStorage asyncTaskStorage,
            @Qualifier("taskExecutor") Executor taskExecutor
    ) {
        this.asyncTaskStorage = asyncTaskStorage;
        this.taskExecutor = taskExecutor;
    }

    @Override
    @Async("taskExecutor")
    public void executeTask(String taskId) {
        runTask(taskId);
    }

    @Override
    public CompletableFuture<Void> executeTaskFuture(String taskId) {
        return CompletableFuture.runAsync(() -> runTask(taskId), taskExecutor);
    }

    private void runTask(String taskId) {
        log.info("Task {} started", taskId);
        try {
            asyncTaskStorage.updateTask(taskId, TaskStatus.IN_PROGRESS);
            Thread.sleep(TASK_EXECUTION_TIME_MILLIS);
            asyncTaskStorage.updateTask(taskId, TaskStatus.DONE);
            log.info("Task {} finished successfully", taskId);
        } catch (InterruptedException ex) {
            log.error("Task {} interrupted", taskId);
            asyncTaskStorage.updateTask(taskId, TaskStatus.FAILED);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("Task {} failed", taskId, ex);
            asyncTaskStorage.updateTask(taskId, TaskStatus.FAILED);
        }
    }
}
