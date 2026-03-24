package com.ct240.backend.controller;

import com.ct240.backend.dto.request.SpaceCreationRequest;
import com.ct240.backend.dto.request.SpaceUpdateRequest;
import com.ct240.backend.dto.response.ApiResponse;
import com.ct240.backend.dto.response.SpaceResponse;
import com.ct240.backend.service.SpaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spaces")
public class SpaceController {
    @Autowired
    SpaceService spaceService;

    @PostMapping
    ApiResponse<SpaceResponse> createSpace(@RequestBody @Valid SpaceCreationRequest request, Authentication authentication){
        ApiResponse<SpaceResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(spaceService.createSpace(request, authentication));

        return apiResponse;
    }
    @GetMapping("/{spaceId}")
    ApiResponse<SpaceResponse> getSpace(@PathVariable String spaceId, Authentication authentication){
        ApiResponse<SpaceResponse> apiResponse = new ApiResponse<>();

        SpaceResponse response = spaceService.getSpace(spaceId, authentication);

        apiResponse.setData(response);

        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<SpaceResponse>> getAllSpaces(Authentication authentication){
        ApiResponse<List<SpaceResponse>> apiResponse = new ApiResponse<>();

        apiResponse.setData(spaceService.getAllSpaces(authentication));

        return apiResponse;
    }

    @PutMapping("/{spaceId}")
    ApiResponse<SpaceResponse> updateSpace(@PathVariable String spaceId, @RequestBody @Valid SpaceUpdateRequest request, Authentication authentication){
        ApiResponse<SpaceResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(spaceService.updateSpace(spaceId, request, authentication));

        return apiResponse;
    }

    @DeleteMapping("/{spaceId}")
    ApiResponse<Void> deleteSpace(@PathVariable String spaceId, Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();

        spaceService.deleteSpace(spaceId, authentication);

        apiResponse.setMessage("Space deleted successfully");

        return  apiResponse;
    }
}
