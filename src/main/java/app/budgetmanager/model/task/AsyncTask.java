package app.budgetmanager.model.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsyncTask {

    private String id;
    private Long userId;
    private TaskStatus status;

    public AsyncTask(String id, Long userId) {
        this.id = id;
        this.userId = userId;
        this.status = TaskStatus.PENDING;
    }
}
