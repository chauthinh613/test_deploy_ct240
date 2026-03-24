package com.ct240.backend.dto.response;

import com.ct240.backend.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpaceMemberResponse {
    UserResponse userResponse;
    Role role;
}
