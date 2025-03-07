package com.one.social_project.domain.notifications.repository;

import com.one.social_project.domain.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> , NotificationRepositoryCustom {
    Optional<Notification> findByNotificationId(String notificationId);

    int deleteByNotificationId(String notificationId);
}
