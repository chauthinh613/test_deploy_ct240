package com.ct240.backend.mapper;

import com.ct240.backend.dto.response.TaskAssignmentResponse;
import com.ct240.backend.entity.TaskAssignment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskAssignmentMapper {
    TaskAssignmentResponse toResponse(TaskAssignment taskAssignment);
}
