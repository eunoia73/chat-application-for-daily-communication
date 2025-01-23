package com.one.social_project.domain.notifications.repository;

import com.one.social_project.domain.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
