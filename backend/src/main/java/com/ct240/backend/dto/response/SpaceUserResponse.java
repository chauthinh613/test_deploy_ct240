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
public class SpaceUserResponse {
    String userId;
    String spaceId;
    Role role;
}
