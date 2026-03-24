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
public class CardResponse {
    String id;
    String boardId;
    String name;
    int position;
    Date createAt;
}
