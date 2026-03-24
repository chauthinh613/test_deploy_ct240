package com.ct240.backend.mapper;

import com.ct240.backend.dto.request.CompleteTaskRequest;
import com.ct240.backend.dto.request.MoveTaskRequest;
import com.ct240.backend.dto.request.TaskCreationRequest;
import com.ct240.backend.dto.request.TaskUpdateRequest;
import com.ct240.backend.dto.response.TaskResponse;
import com.ct240.backend.entity.Task;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    Task toTask(TaskCreationRequest request);
    @Mapping(source = "card.id", target = "cardId")
    TaskResponse toTaskResponse (Task task);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTask (@MappingTarget Task task, TaskUpdateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTask (@MappingTarget Task task, MoveTaskRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTask (@MappingTarget Task task, CompleteTaskRequest request);
}
