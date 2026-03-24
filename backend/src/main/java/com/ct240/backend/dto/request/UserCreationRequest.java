package com.ct240.backend.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank
    @Size(min = 4)
    String username;
    @NotBlank
    @Size(min = 8)
    String password;
    @NotBlank
    String name;
}
