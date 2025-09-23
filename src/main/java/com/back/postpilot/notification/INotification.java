package com.back.postpilot.notification;

import com.back.postpilot.DTO.UserNotifyDTO;

public interface INotification {
    /**
     * Send a notification to the given user for the given post.
     *
     * @param user   The user who should receive the notification
     * @param postId The ID of the post/event that triggered the notification
     */
    void send(UserNotifyDTO user, NotificationEvent event);
}
