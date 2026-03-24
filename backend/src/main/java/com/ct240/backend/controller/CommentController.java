package com.ct240.backend.controller;

import com.ct240.backend.dto.request.CommentCreationRequest;
import com.ct240.backend.dto.response.ApiResponse;
import com.ct240.backend.dto.response.CommentResponse;
import com.ct240.backend.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.security.web.server.ui.OneTimeTokenSubmitPageGeneratingWebFilter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class CommentController {
    @Autowired
    CommentService commentService;

    @PostMapping ("/tasks/{taskId}/comments")
    public ApiResponse<CommentResponse> createComment(
            @PathVariable String taskId,
            @RequestBody @Valid CommentCreationRequest request,
            Authentication authentication){
        ApiResponse<CommentResponse> apiResponse = new ApiResponse<>();
        apiResponse.setData(commentService.createComment(taskId, request, authentication));

        return apiResponse;
    }

    @GetMapping ("/tasks/{taskId}/comments")
    public ApiResponse<List<CommentResponse>> getAllComments(
            @PathVariable String taskId,
            Authentication authentication){
        ApiResponse<List<CommentResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setData(commentService.getAllComments(taskId, authentication));

        return apiResponse;
    }
    @DeleteMapping ("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable String commentId,
            Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        commentService.deleteComment(commentId, authentication);
        apiResponse.setMessage("Task is Deleted");
        return apiResponse;
    }
}
