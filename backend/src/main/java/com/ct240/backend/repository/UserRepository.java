package com.ct240.backend.repository;

import com.ct240.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    List<User> findByUsernameContaining(String username);
    Optional<User> findByUsername(String username);

    // Trong UserRepository thêm:
    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Optional<String> findIdByUsername(@Param("username") String username);
}
