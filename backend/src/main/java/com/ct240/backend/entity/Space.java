package com.ct240.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE )
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    String description;
    Date createAt;

    @OneToMany(mappedBy = "space",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    List<Board> boards;

    @OneToMany(mappedBy = "space",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    List<SpaceUser> spaceUserList;

}
