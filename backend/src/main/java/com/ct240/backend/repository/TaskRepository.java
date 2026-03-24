package com.ct240.backend.repository;

import com.ct240.backend.entity.Board;
import com.ct240.backend.entity.Task;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {
    List <Task> findByCardId (String cardId);

    @Query("SELECT b " +
            "FROM Board b " +
            "JOIN Card c ON b.id = c.board.id " +
            "JOIN Task t ON c.id = t.card.id " +
            "WHERE t.id = :taskId")
    Board findBoardByTaskId(@Param("taskId") String  taskId);

    @Query("SELECT MAX(t.position) FROM Task t WHERE t.card.id = :cardId")
    Integer findMaxPositionByCardId(@Param("cardId") String cardId);

}
