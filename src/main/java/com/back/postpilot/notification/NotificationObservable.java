package com.back.postpilot.notification;

import com.back.postpilot.DTO.UserNotifyDTO;
import com.back.postpilot.EnumTypeConstants.NotificationType;
import com.back.postpilot.entity.UserNotificationEntity;
import com.back.postpilot.repository.UserRepository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationObservable {
    private final List<NotificationObserver> observers = new CopyOnWriteArrayList<>();
    private final UserRepository userRepository;

    // Constructor injection
    public NotificationObservable(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(NotificationEvent event) {
        for (NotificationObserver observer : observers) {
            observer.onNotify(event);
        }
    }

    public void clearObservers() {
        observers.clear();
    }
    public void refreshObservers() {
        List<UserNotifyDTO> users = userRepository.findAll().stream()
                .map(user -> {
                    List<NotificationType> activeTypes = user.getNotificationTypes().stream()
                            .filter(UserNotificationEntity::getShouldNotify)
                            .map(UserNotificationEntity::getNotificationType)
                            .toList();

                    return new UserNotifyDTO(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            activeTypes
                    );
                })
                .toList();


        observers.clear();
        for (UserNotifyDTO user : users) {
            observers.add(new UserNotificationObserver(user));
        }
    }
}
