package com.ct240.backend.repository;

import com.ct240.backend.entity.Board;
import com.ct240.backend.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, String> {
    List<Board> findBySpaceId(String spaceId);
    List<Board> findBySpaceIdAndIsPrivateFalse(String spaceId);

    @Query("SELECT b.isPrivate FROM Board b WHERE b.id = :boardId")
    Boolean isPrivate(@Param("boardId") String boardId);

    @Query("SELECT b.space " +
            "FROM Board b " +
            "WHERE b.id = :boardId")
    Space findSpaceByBoardId(@Param("boardId") String boardId);

    @Query("SELECT b " +
            "FROM BoardUser bu " +
            "JOIN bu.board b " +
            "WHERE b.space.id = :spaceId " +
            "AND bu.user.id = :userId " +
            "AND b.isPrivate = true")
    List<Board> findBySpaceIdAndIsPrivateTrue(@Param("spaceId") String spaceId, @Param("userId") String userId);
}
