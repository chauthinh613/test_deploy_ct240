package com.ct240.backend.repository;

import com.ct240.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByTaskId(String taskId);
}
