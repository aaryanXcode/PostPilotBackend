package com.back.postpilot.DTO;

import com.back.postpilot.EnumTypeConstants.NotificationType;

import java.util.List;

public class UserNotifyDTO {

    private Long userId;
    private String name;
    private String email;

    // List of notification types the user wants to receive
    private List<NotificationType> notificationTypes;

    // Optional: map type -> shouldNotify flag, if you want per-type control
    // private Map<NotificationType, Boolean> shouldNotifyMap;

    public UserNotifyDTO() {}

    public UserNotifyDTO(Long userId, String name, String email, List<NotificationType> notificationTypes) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.notificationTypes = notificationTypes;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<NotificationType> getNotificationTypes() { return notificationTypes; }
    public void setNotificationTypes(List<NotificationType> notificationTypes) { this.notificationTypes = notificationTypes; }
}
