package com.back.postpilot.notification;

import com.back.postpilot.DTO.UserNotifyDTO;

public class NotificationEvent {
    private final Long postId;

    public NotificationEvent(Long postId) {
        this.postId = postId;
    }

    public Long getPostId() {
        return postId;
    }
}
