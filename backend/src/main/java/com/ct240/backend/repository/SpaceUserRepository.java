package com.ct240.backend.repository;

import com.ct240.backend.entity.Space;
import com.ct240.backend.entity.SpaceUser;
import com.ct240.backend.entity.User;
import com.ct240.backend.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpaceUserRepository extends JpaRepository<SpaceUser, String> {

    boolean existsByUserIdAndSpaceId(String userId, String spaceId);
    boolean existsByUserIdAndSpaceIdAndRole(String userId, String spaceId, Role role);

    Optional<SpaceUser> findByUserIdAndSpaceId(String userId, String spaceId);

    @Query("SELECT su FROM SpaceUser su " +
            "JOIN su.space s " +
            "JOIN Board b ON b.space.id = s.id " +
            "WHERE su.user.id = :userId AND b.id = :boardId")
    Optional<SpaceUser> findByUserIdAndBoardId(@Param("userId") String userId,
                                               @Param("boardId") String boardId);

    @Query("SELECT su.user FROM SpaceUser su WHERE su.space.id = :spaceId")
    List<User> findUsersBySpaceId(@Param("spaceId") String spaceId);
}
