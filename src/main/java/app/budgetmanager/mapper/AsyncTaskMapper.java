package app.budgetmanager.mapper;

import app.budgetmanager.dto.AsyncTaskResponseDto;
import app.budgetmanager.model.task.AsyncTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AsyncTaskMapper {

    @Mapping(target = "taskId", source = "id")
    @Mapping(target = "taskStatus", source = "status")
    AsyncTaskResponseDto toDto(AsyncTask task);
}
