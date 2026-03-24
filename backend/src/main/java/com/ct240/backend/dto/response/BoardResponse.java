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
public class BoardResponse {
    String id;
    String spaceId;
    String name;
    String description;
    boolean isPrivate;
    Date createAt;
}
