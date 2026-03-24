package com.ct240.backend.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntrospectRequest {
    private String token;
}
