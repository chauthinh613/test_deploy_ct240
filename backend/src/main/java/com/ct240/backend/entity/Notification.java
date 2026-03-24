package com.ct240.backend.entity;

import com.ct240.backend.enums.Type;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE )
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String content;
    boolean readStatus;
    @Enumerated(EnumType.STRING)
    Type type;
    String referenceId;
    Date createAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
