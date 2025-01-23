package com.one.social_project.domain.notifications.repository;

import com.one.social_project.domain.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiver(String receiver);

}
