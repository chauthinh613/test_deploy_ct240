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
public class TaskResponse {
    String id;
    String cardId;
    String name;
    String description;
    boolean completed;
    Date deadline;
    int position;
    Date createAt;

}
