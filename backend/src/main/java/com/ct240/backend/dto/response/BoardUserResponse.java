package com.ct240.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BoardUserResponse {
    String boardId;
    String userId;
    boolean isOwner;
}
