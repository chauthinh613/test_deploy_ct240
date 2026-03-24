package com.ct240.backend.controller;

import com.ct240.backend.dto.request.AuthRequest;
import com.ct240.backend.dto.request.IntrospectRequest;
import com.ct240.backend.dto.request.UserCreationRequest;
import com.ct240.backend.dto.response.*;
import com.ct240.backend.service.AuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @PostMapping("/register")
    ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest request){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();

        //authService.register(request);

        apiResponse.setData(authService.register(request));
        apiResponse.setMessage(SuccessMessage.REGISTER_SUCCESS.getMessage());

        return apiResponse;
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@RequestBody @Valid AuthRequest request){
        var result = authService.login(request);

        return ApiResponse.<AuthResponse>builder()
                .code(1000)
                .message(SuccessMessage.LOGIN_SUCCESS.getMessage())
                .data(result)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> login(@RequestBody @Valid IntrospectRequest request) throws ParseException, JOSEException {
        var result = authService.introspect(request);

        return ApiResponse.<IntrospectResponse>builder()
                .code(1000)
                .data(result)
                .build();
    }

}
