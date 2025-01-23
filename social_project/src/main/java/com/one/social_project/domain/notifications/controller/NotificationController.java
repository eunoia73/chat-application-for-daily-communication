package com.one.social_project.domain.notifications.controller;

import com.one.social_project.domain.notifications.entity.Notification;
import com.one.social_project.domain.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

//    //채팅방 생성시
//    public Notification sendNotifications(){
//
//        return
//    }


    //public void generateNotifications(RoomResponse response) {
    //    List<Long> participants = response.getParticipants();
    //    for (Long userId : participants) {
    //        String message = String.format("You have been invited to join the chat room: '%s'.", response.getRoomName());
    //        Notification notification = new Notification(userId, message, response.getRoomId());
    //        notificationService.saveNotification(notification); // 알림 저장
    //    }
    //}
}
