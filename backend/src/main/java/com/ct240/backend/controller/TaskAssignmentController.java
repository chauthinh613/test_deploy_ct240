package com.ct240.backend.controller;

import com.ct240.backend.dto.request.TaskAssignmentRequest;
import com.ct240.backend.dto.response.ApiResponse;
import com.ct240.backend.dto.response.TaskAssignmentResponse;
import com.ct240.backend.dto.response.UserResponse;
import com.ct240.backend.service.TaskAssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TaskAssignmentController {

    @Autowired
    TaskAssignmentService taskAssignmentService;

    @PostMapping("/tasks/{taskId}/assign")
    ApiResponse<TaskAssignmentResponse> assignTask(@PathVariable String taskId, @RequestBody @Valid TaskAssignmentRequest request, Authentication authentication){
        ApiResponse<TaskAssignmentResponse> apiResponse = new ApiResponse<>();

        var data = taskAssignmentService.assignTask(taskId, request, authentication);

        apiResponse.setData(data);

        return apiResponse;
    }

    @GetMapping("/tasks/{taskId}/assignments")
    ApiResponse<List<UserResponse>> getAllUsersInTask(@PathVariable String taskId, Authentication authentication){
        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();

        var data = taskAssignmentService.getAllUsersInTask(taskId, authentication);

        apiResponse.setData(data);

        return apiResponse;
    }

    @DeleteMapping("/tasks/{taskId}/assign/{userId}")
    ApiResponse<Void> unassignTask(@PathVariable String taskId, @PathVariable String userId, Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();

        taskAssignmentService.unassignTask(taskId, userId, authentication);

        apiResponse.setMessage("User unassigned from task successfully");

        return apiResponse;
    }

}
