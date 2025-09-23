package com.back.postpilot.notification;

import com.back.postpilot.EnumTypeConstants.NotificationType;

import java.util.EnumMap;
import java.util.Map;

public class NotificationFactory {

    private static final Map<NotificationType, INotification> notificationMap = new EnumMap<>(NotificationType.class);

    static {
        notificationMap.put(NotificationType.EMAIL, new EmailNotification());
        notificationMap.put(NotificationType.SMS, new SMSNotification());
        notificationMap.put(NotificationType.PUSH, new PushNotification());
    }

    public static INotification getNotification(NotificationType type) {
        return notificationMap.get(type);
    }
}
