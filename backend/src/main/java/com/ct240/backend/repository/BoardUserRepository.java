package com.ct240.backend.repository;

import com.ct240.backend.entity.Board;
import com.ct240.backend.entity.BoardUser;
import com.ct240.backend.entity.User;
import com.ct240.backend.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardUserRepository extends JpaRepository<BoardUser, String> {
    boolean existsByUserIdAndBoardId (String userId, String boardId);
//    boolean exitsByUserIdAndSpaceId (String userId, String spaceId);
    boolean existsByUserIdAndBoardIdAndIsOwner(String userId, String boardId, boolean isOwner);

    Optional<BoardUser> findByUserIdAndBoardId(String userId, String boardId);

    Optional<BoardUser> findByBoardIdAndIsOwner(String boardId, boolean isOwner);

    @Query("SELECT bu.user FROM BoardUser bu WHERE bu.board.id = :boardId")
    List<User> findUsersByBoardId(@Param("boardId") String boardId);

    @Query("SELECT bu FROM BoardUser bu WHERE bu.user.id = :userId AND bu.board.space.id = :spaceId")
    List<BoardUser> findAllByUserIdAndSpaceId(String userId, String spaceId);

}
