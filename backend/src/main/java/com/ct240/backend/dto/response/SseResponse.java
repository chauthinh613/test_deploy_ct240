package com.ct240.backend.dto.response;

import com.ct240.backend.enums.Type;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SseResponse {
    @Builder.Default
    int code = 2000;
    Type type;
    String message;
    String spaceId;
}
