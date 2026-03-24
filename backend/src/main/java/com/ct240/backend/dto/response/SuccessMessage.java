package com.ct240.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum SuccessMessage {
    LOGIN_SUCCESS("Login Success"),
    REGISTER_SUCCESS("Register Success");
    private String message;
}
