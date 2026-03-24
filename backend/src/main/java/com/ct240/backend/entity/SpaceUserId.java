package com.ct240.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SpaceUserId implements Serializable {
    @Column(name = "space_id")
    private String spaceId;

    @Column(name = "user_id")
    private String userId;
}
