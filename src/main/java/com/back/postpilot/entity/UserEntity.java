package com.back.postpilot.entity;

import jakarta.persistence.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // USER, ADMIN, SUPER_ADMIN

    // Constructors
    public UserEntity() {}

    public UserEntity(String username, String password, String email, String name, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserNotificationEntity> notificationTypes = new HashSet<>();

    public Set<UserNotificationEntity> getNotificationTypes() {
        return notificationTypes;
    }

    public void setNotificationTypes(Set<UserNotificationEntity> notificationTypes) {
        this.notificationTypes = notificationTypes;
    }

    public void addNotificationType(UserNotificationEntity notification) {
        notification.setUserEntity(this);
        this.notificationTypes.add(notification);
    }

    public void removeNotificationType(UserNotificationEntity notification) {
        notification.setUserEntity(null);
        this.notificationTypes.remove(notification);
    }

}