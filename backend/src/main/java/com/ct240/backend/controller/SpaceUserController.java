package com.ct240.backend.controller;

import com.ct240.backend.dto.request.SpaceUserRequest;
import com.ct240.backend.dto.request.SpaceUserUpdateRequest;
import com.ct240.backend.dto.response.*;
import com.ct240.backend.entity.SpaceUser;
import com.ct240.backend.service.SpaceUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spaces")
public class SpaceUserController {
    @Autowired
    SpaceUserService spaceUserService;


    @PostMapping("/{spaceId}/members")
    public ApiResponse<SpaceUserResponse> addMember(@PathVariable String spaceId, @RequestBody @Valid SpaceUserRequest request, Authentication authentication){
        ApiResponse<SpaceUserResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(spaceUserService.addMember(spaceId, request, authentication));

        return apiResponse;
    }

    @GetMapping("/{spaceId}/members")
    public ApiResponse<List<SpaceMemberResponse>> getAllMembersInSpace(@PathVariable String spaceId, Authentication authentication){
        ApiResponse<List<SpaceMemberResponse>> apiResponse = new ApiResponse<>();

        var data = spaceUserService.getAllMembersInSpace(spaceId, authentication);

        apiResponse.setData(data);

        return  apiResponse;
    }

    @PutMapping("/{spaceId}/members/{userId}")
    public ApiResponse<SpaceUserResponse> updateRole(@PathVariable String spaceId, @PathVariable String userId, @RequestBody @Valid SpaceUserUpdateRequest request, Authentication authentication){
        ApiResponse<SpaceUserResponse> apiResponse = new ApiResponse<>();

        var data = spaceUserService.updateRole(spaceId, userId, request, authentication);

        apiResponse.setData(data);

        return  apiResponse;
    }

    /// một người nào đó xoá người khác
    @DeleteMapping("/{spaceId}/members/{userId}")
    public ApiResponse<Void> deleteUserFromSpace(@PathVariable String spaceId, @PathVariable String userId, Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();

        spaceUserService.deleteUserFromSpace(spaceId, userId, authentication);

        apiResponse.setMessage("User removed from the space successfully");

        return  apiResponse;
    }

    ///tự rời khỏi space
    @DeleteMapping("/{spaceId}/members")
    public ApiResponse<Void> deleteUserFromSpace(@PathVariable String spaceId, Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();

        spaceUserService.deleteUserFromSpace(spaceId, authentication);

        apiResponse.setMessage("User left the space successfully");

        return  apiResponse;
    }
}
