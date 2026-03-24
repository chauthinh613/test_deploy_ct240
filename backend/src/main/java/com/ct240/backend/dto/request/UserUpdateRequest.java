package com.ct240.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @NotBlank
    String name;
    String avatarURL;

    // chuyển qua updatePassword nếu có thay đổi mật khẩu
//    @Size(min = 8)
//    String password;
}
