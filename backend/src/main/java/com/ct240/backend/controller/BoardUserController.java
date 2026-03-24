package com.ct240.backend.controller;

import com.ct240.backend.dto.request.BoardUserRequest;
import com.ct240.backend.dto.response.ApiResponse;
import com.ct240.backend.dto.response.BoardMemberResponse;
import com.ct240.backend.dto.response.BoardUserResponse;
import com.ct240.backend.dto.response.UserResponse;
import com.ct240.backend.service.BoardUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BoardUserController {
    @Autowired
    BoardUserService boardUserService;

    @PostMapping("/boards/{boardId}/members")
    ApiResponse<BoardUserResponse> addMember(@PathVariable String boardId, @RequestBody @Valid BoardUserRequest request, Authentication authentication){
        ApiResponse<BoardUserResponse> apiResponse = new ApiResponse<>();

        var data = boardUserService.addMember(boardId, request, authentication);

        apiResponse.setData(data);

        return apiResponse;
    }

    @GetMapping("/boards/{boardId}/members")
    ApiResponse<List<BoardMemberResponse>> getAllUsersInBoard(@PathVariable String boardId, Authentication authentication){
        ApiResponse<List<BoardMemberResponse>> apiResponse = new ApiResponse<>();

        var data = boardUserService.getAllUsersInBoard(boardId, authentication);

        apiResponse.setData(data);

        return apiResponse;
    }


    //người khác xoá
    @DeleteMapping("/boards/{boardId}/members/{userId}")
    ApiResponse<Void> deleteUser(@PathVariable String boardId, @PathVariable String userId, Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();

        boardUserService.deleteUserFromBoard(boardId, userId, authentication);

        apiResponse.setMessage("User removed from the board successfully");

        return apiResponse;
    }

    //tự rời
    @DeleteMapping("/boards/{boardId}/members")
    ApiResponse<Void> deleteUser(@PathVariable String boardId, Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();

        boardUserService.deleteUserFromBoard(boardId, authentication);

        apiResponse.setMessage("User left the board successfully");

        return apiResponse;
    }
}
