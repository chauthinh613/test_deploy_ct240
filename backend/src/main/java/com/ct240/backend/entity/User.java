package com.ct240.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE )
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String username;
    String password;
    String name;
    @Column(name = "avatar_url")
    String avatarURL ;


    @OneToMany(mappedBy = "user")
    List<TaskAssignment> taskAssignmentList;

    @OneToMany(mappedBy = "user")
    List<SpaceUser> spaceUsersList;

    @OneToMany(mappedBy = "user")
    List<Comment> comments;

    @OneToMany(mappedBy = "user")
    List<BoardUser> boardUsersList;

    @OneToMany(mappedBy = "user")
    List<Notification> notificationList;
}
