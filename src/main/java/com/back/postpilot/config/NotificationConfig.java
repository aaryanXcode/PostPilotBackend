package com.back.postpilot.config;

import com.back.postpilot.DTO.UserNotifyDTO;
import com.back.postpilot.notification.NotificationObservable;
import com.back.postpilot.notification.UserNotificationObserver;
import com.back.postpilot.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NotificationConfig {

    @Bean
    public NotificationObservable notificationObservable(UserRepository userRepository) {
        return new NotificationObservable(userRepository);
    }
}
