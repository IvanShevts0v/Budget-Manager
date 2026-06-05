package app.budgetmanager.util;

import java.util.concurrent.CompletableFuture;

public interface AsyncTaskRunner {

    void executeTask(String taskId);

    CompletableFuture<Void> executeTaskFuture(String taskId);
}
