package com.ct240.backend.repository;

import com.ct240.backend.entity.TaskAssignment;
import com.ct240.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, String> {

    @Query("SELECT ta.user FROM TaskAssignment ta WHERE ta.task.id = :taskId")
    List<User> findAllUsersByTaskId(@Param("taskId") String taskId);

    Optional<TaskAssignment> findByTaskIdAndUserId(String taskId, String userId);
}
