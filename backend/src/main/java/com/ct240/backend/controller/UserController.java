package com.ct240.backend.controller;

import com.ct240.backend.dto.request.ChangePasswordRequest;
import com.ct240.backend.dto.request.UserCreationRequest;
import com.ct240.backend.dto.request.UserUpdateRequest;
import com.ct240.backend.dto.response.ApiResponse;
import com.ct240.backend.dto.response.UserResponse;
import com.ct240.backend.entity.User;
import com.ct240.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping
    ApiResponse <List<UserResponse>> getAllUsers(){
        ApiResponse <List<UserResponse>> apiResponse = new ApiResponse<>();

        apiResponse.setData(userService.getAllUsers());

        return apiResponse;
    }

    @GetMapping("/profile")
    ApiResponse<UserResponse> getUser(Authentication authentication){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();

        String username = authentication.getName();

        apiResponse.setData(userService.getUserByUsername(username));

        return apiResponse;
    }

    @PutMapping("/update")
    ApiResponse<UserResponse> updateUser(@RequestBody @Valid UserUpdateRequest request, Authentication authentication){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();


        apiResponse.setData(userService.updateUser(request, authentication));

        return apiResponse;
    }

    @GetMapping("/search")
    ApiResponse<List<UserResponse>> searchUsers(@RequestParam String keyword){
        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();

        apiResponse.setData(userService.searchUsers(keyword));

        return apiResponse;
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable String userId){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(userService.getUser(userId));

        return apiResponse;
    }

    @PutMapping("/update/password")
    ApiResponse<Void> updatePassword(@RequestBody @Valid ChangePasswordRequest request, Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();

        userService.updatePassword(request, authentication);

        apiResponse.setMessage("Change Successfully");

        return  apiResponse;
    }

    @PostMapping("/avatar")
    ApiResponse<UserResponse> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(userService.uploadAvatar(file, authentication));
        apiResponse.setMessage("Changing avatar is successful");
        return apiResponse;
    }

    @DeleteMapping("/avatar")
    ApiResponse<UserResponse> deleteAvatar (Authentication authentication){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(userService.deleteAvatar(authentication));
        apiResponse.setMessage("Delete current avatar");
        return apiResponse;
    }



    /// tạo người dùng đem qua AuthService ///
//    @PostMapping
//    ApiResponse <UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){
//        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
//
//        apiResponse.setData(userService.createUser(request));
//
//        return apiResponse;
//    }



}
