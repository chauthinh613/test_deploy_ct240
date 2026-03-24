package com.ct240.backend.dto.request;

import com.ct240.backend.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpaceUserRequest {
    @NotNull
    String userId;
    @NotNull
    Role role;
}
