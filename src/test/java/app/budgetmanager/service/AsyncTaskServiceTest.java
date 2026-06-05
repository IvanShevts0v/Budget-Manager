package app.budgetmanager.service;

import app.budgetmanager.dto.AsyncTaskResponseDto;
import app.budgetmanager.mapper.AsyncTaskMapper;
import app.budgetmanager.model.entity.User;
import app.budgetmanager.model.task.AsyncTask;
import app.budgetmanager.model.task.TaskStatus;
import app.budgetmanager.repository.UserRepository;
import app.budgetmanager.util.AsyncTaskRunner;
import app.budgetmanager.util.AsyncTaskStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncTaskServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private AsyncTaskMapper mapper;

    @Mock
    private AsyncTaskRunner asyncTaskRunner;

    @Mock
    private UserRepository userRepository;

    private AsyncTaskStorage storage;
    private AsyncTaskService service;

    @BeforeEach
    void setUp() {
        storage = new AsyncTaskStorage();
        service = new AsyncTaskService(storage, mapper, asyncTaskRunner, userRepository);
    }

    @Test
    void startTaskShouldCreateTaskAndExecuteAsync() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(mapper.toDto(any(AsyncTask.class))).thenAnswer(invocation -> {
            AsyncTask task = invocation.getArgument(0);
            return new AsyncTaskResponseDto(task.getId(), task.getUserId(), task.getStatus());
        });

        AsyncTaskResponseDto result = service.startTask(USER_ID);

        assertEquals(USER_ID, result.getUserId());
        assertEquals(TaskStatus.PENDING, result.getTaskStatus());
        verify(asyncTaskRunner).executeTask(result.getTaskId());
    }

    @Test
    void startTaskWithCompletableFutureShouldScheduleTask() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(mapper.toDto(any(AsyncTask.class))).thenAnswer(invocation -> {
            AsyncTask task = invocation.getArgument(0);
            return new AsyncTaskResponseDto(task.getId(), task.getUserId(), task.getStatus());
        });
        when(asyncTaskRunner.executeTaskFuture(anyString())).thenReturn(CompletableFuture.completedFuture(null));

        AsyncTaskResponseDto result = service.startTaskWithCompletableFuture(USER_ID);

        assertEquals(USER_ID, result.getUserId());
        verify(asyncTaskRunner).executeTaskFuture(result.getTaskId());
    }

    @Test
    void getByIdShouldReturnTaskForSameUser() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        AsyncTask created = storage.create(USER_ID);
        created.setStatus(TaskStatus.IN_PROGRESS);
        when(mapper.toDto(created)).thenReturn(
                new AsyncTaskResponseDto(created.getId(), USER_ID, TaskStatus.IN_PROGRESS)
        );

        AsyncTaskResponseDto result = service.getById(created.getId(), USER_ID);

        assertEquals(TaskStatus.IN_PROGRESS, result.getTaskStatus());
    }

    @Test
    void getByIdShouldThrowForbiddenForDifferentUser() {
        User user = new User();
        user.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        AsyncTask created = storage.create(USER_ID);

        assertThrows(ResponseStatusException.class, () -> service.getById(created.getId(), 2L));
    }

    @Test
    void getByIdShouldThrowNotFoundWhenTaskMissing() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> service.getById("missing", USER_ID));
    }

    @Test
    void startTaskShouldThrowWhenUserMissing() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.startTask(USER_ID));

        verify(asyncTaskRunner, never()).executeTask(anyString());
        verifyNoMoreInteractions(asyncTaskRunner, mapper);
    }
}
