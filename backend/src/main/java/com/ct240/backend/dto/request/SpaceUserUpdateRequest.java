package com.ct240.backend.dto.request;

import com.ct240.backend.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.*;
import lombok.experimental.FieldDefaults;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpaceUserUpdateRequest {
    ////UpdateRoleRequest
    @NotNull
    Role role;
}
