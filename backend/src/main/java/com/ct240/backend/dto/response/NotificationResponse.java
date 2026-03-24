package com.ct240.backend.dto.response;

import com.ct240.backend.enums.Type;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    String id;
    String content;
    boolean readStatus;
    Type type;
    String referenceId;
    Date createAt;
}
