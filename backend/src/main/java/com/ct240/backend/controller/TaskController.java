package com.ct240.backend.controller;

import com.ct240.backend.dto.request.*;
import com.ct240.backend.dto.response.ApiResponse;
import com.ct240.backend.dto.response.CardResponse;
import com.ct240.backend.dto.response.TaskResponse;
import com.ct240.backend.entity.Task;
import com.ct240.backend.service.TaskService;
import jakarta.validation.Valid;
import org.hibernate.query.sqm.mutation.internal.TableKeyExpressionCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TaskController {
    @Autowired
    TaskService taskService;

    @PostMapping ("/cards/{cardId}/tasks")
    ApiResponse<TaskResponse> createTask (
            @PathVariable String cardId,
            @RequestBody @Valid TaskCreationRequest request,
            Authentication authentication){
        ApiResponse<TaskResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(taskService.createTask(cardId, request, authentication));

        return apiResponse;
    }

    @GetMapping ("/cards/{cardId}/tasks")
    ApiResponse<List<TaskResponse>> getAllTasks(
            @PathVariable String cardId, Authentication authentication){
        ApiResponse<List<TaskResponse>> apiResponse = new ApiResponse<>();

        apiResponse.setData(taskService.getAllTasks(cardId, authentication));
        return apiResponse;

    }

    @PutMapping ("/tasks/{taskId}")
    ApiResponse<TaskResponse> updateTask(
            @PathVariable String taskId,
            @RequestBody @Valid TaskUpdateRequest request,
            Authentication authentication){
        ApiResponse<TaskResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(taskService.updateTask(taskId, request, authentication));

        return apiResponse;
    }

    @PutMapping("/tasks/{taskId}/move")
    public ApiResponse<TaskResponse> moveCard(
            @PathVariable String taskId,
            @RequestBody @Valid MoveTaskRequest request,
            Authentication authentication){
        ApiResponse<TaskResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(taskService.moveTask(taskId, request, authentication));

        return apiResponse;
    }

    /// chỉnh completed của Task
    @PutMapping("/tasks/{taskId}/complete")
    public ApiResponse<TaskResponse> complete(
            @PathVariable String taskId,
            @RequestBody @Valid CompleteTaskRequest request,
            Authentication authentication){
        ApiResponse<TaskResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(taskService.complete(taskId, request, authentication));

        return apiResponse;
    }

    @DeleteMapping ("/tasks/{taskId}")
    ApiResponse<Void> deleteTask(
            @PathVariable String taskId,
            Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();

        taskService.deleteTask(taskId, authentication);
        apiResponse.setMessage("Task is deleted successfully");

        return apiResponse;

    }


}
