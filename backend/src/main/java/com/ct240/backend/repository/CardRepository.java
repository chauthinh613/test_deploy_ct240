package com.ct240.backend.repository;

import com.ct240.backend.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, String> {
    List <Card> findByBoardId(String boardId);

    @Query("SELECT MAX(c.position) FROM Card c WHERE c.board.id = :boardId")
    Integer findMaxPositionByBoardId(@Param("boardId") String boardId);


}
