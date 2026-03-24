package com.ct240.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)//generate qua JS, truong nao null => khong hien thi
public class ApiResponse<T> {
    private int code = 1000;
    private String message;
    private T data;
}
