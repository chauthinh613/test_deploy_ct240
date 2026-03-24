package com.ct240.backend.dto.response;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpaceResponse {
    private String id;
    private String name;
    private String description;
    private Date createAt;
}
