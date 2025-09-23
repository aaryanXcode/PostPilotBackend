package com.back.postpilot.notification;


import com.back.postpilot.DTO.UserNotifyDTO;


public class SMSNotification implements INotification {

    @Override
    public void send(UserNotifyDTO user, NotificationEvent event) {
        System.out.println("📱 Sending SMS to " + "phone" + " for Post ID: " + event.getPostId());
    }
}
