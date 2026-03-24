package com.ct240.backend.repository;

import com.ct240.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserId(String userId);

    boolean existsByIdAndUserId(String notificationId, String userId);
}
