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
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    Date createAt;
    int position;

    @ManyToOne
    @JoinColumn(name = "board_id")
    Board board;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Task> tasks;
}
