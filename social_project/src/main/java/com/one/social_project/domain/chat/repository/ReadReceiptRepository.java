package com.one.social_project.domain.chat.repository;

import com.one.social_project.domain.chat.entity.ReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadReceiptRepository extends JpaRepository<ReadReceipt, Long> {

    boolean existsByMessageIdAndUserId(String messageId, String userId);
}
