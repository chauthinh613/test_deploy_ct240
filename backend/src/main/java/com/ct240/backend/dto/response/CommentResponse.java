package com.ct240.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String id;
    String taskId;
    String userId;
    String content;
    Date createAt;
    UserResponse user;
}
