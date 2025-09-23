package com.back.postpilot.notification;

import com.back.postpilot.DTO.UserNotifyDTO;


public class EmailNotification implements INotification {

    @Override
    public void send(UserNotifyDTO user, NotificationEvent event) {
        System.out.println("📧 Sending EMAIL to " + user.getEmail() + " for Post ID: " + event.getPostId());
    }
}
