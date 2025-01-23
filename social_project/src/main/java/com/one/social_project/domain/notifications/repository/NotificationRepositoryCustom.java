package com.one.social_project.domain.notifications.repository;

import com.one.social_project.domain.notifications.dto.NotificationListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom {
    Page<NotificationListDTO> findByReceiver(String receiver, Pageable pageable);

}
