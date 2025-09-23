package com.back.postpilot.notification;

public interface NotificationObserver {
    /**
     * Called when a NotificationEvent is triggered.
     * @param event contains the postId
     */
    void onNotify(NotificationEvent event);
}
