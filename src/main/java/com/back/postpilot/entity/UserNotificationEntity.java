package com.back.postpilot.entity;

import com.back.postpilot.EnumTypeConstants.NotificationType;
import jakarta.persistence.*;

@Entity
@Table(name= "user_notification_type")
public class UserNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity userEntity;

    @Column(name = "should_notify", nullable = false)
    private Boolean shouldNotify = true;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Boolean getShouldNotify() {
        return shouldNotify;
    }

    public void setShouldNotify(Boolean shouldNotify) {
        this.shouldNotify = shouldNotify;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }


}
