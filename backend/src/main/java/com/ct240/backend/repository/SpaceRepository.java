package com.ct240.backend.repository;

import com.ct240.backend.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpaceRepository extends JpaRepository<Space, String> {

    List<Space> findAllById(String id);

    @Query("SELECT s FROM Space s JOIN SpaceUser su ON su.space.id = s.id WHERE su.user.id = :userId")
    List<Space> findAllByUserId(@Param("userId") String userId);
}
