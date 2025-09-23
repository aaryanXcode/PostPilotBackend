package com.back.postpilot.notification;

import com.back.postpilot.DTO.UserNotifyDTO;

public class UserNotificationObserver implements NotificationObserver {

    private final UserNotifyDTO user;

    public UserNotificationObserver(UserNotifyDTO user) {
        this.user = user;
    }

    @Override
    public void onNotify(NotificationEvent event) {
        Long postId = event.getPostId();

        for (var type : user.getNotificationTypes()) {
            INotification notification = NotificationFactory.getNotification(type);
            notification.send(user, event);
        }
    }
}
