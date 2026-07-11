package com.saasclient.repository;

import com.saasclient.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop30ByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndLuFalse(Long userId);

    long countByUserIdAndLuFalse(Long userId);
}
