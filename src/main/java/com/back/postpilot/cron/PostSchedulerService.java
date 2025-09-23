package com.back.postpilot.cron;

import com.back.postpilot.DTO.ScheduleRequestDTO;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.notification.NotificationEvent;
import com.back.postpilot.notification.NotificationObservable;
import com.back.postpilot.repository.GeneratedContentRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
public class PostSchedulerService {
    private final GeneratedContentRepository generatedContentRepository;
    private final TaskScheduler taskScheduler;
    private final NotificationObservable notificationObservable;
    private final Map<Long, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    public PostSchedulerService(TaskScheduler taskScheduler, GeneratedContentRepository generatedContentRepository, NotificationObservable notificationObservable){
        this.taskScheduler = taskScheduler;
        this.generatedContentRepository = generatedContentRepository;
        this.notificationObservable = notificationObservable;
    }

    public void schedulePostNotification(ScheduleRequestDTO requestDTO) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduleTime = LocalDateTime.parse(requestDTO.dateTime());
        if (scheduleTime.isAfter(now)) {
            Duration delay = Duration.between(now, scheduleTime);
            if (scheduleTime.isAfter(LocalDateTime.now())) {
                cancelScheduledPost(requestDTO.id());
                Runnable task = () -> {
                    try {
                        //notify user
                        NotificationEvent event = new NotificationEvent(requestDTO.id());
                        notificationObservable.refreshObservers();
                        notificationObservable.notifyObservers(event);
                        generatedContentRepository.updateScheduleFalse(requestDTO.id());
                        tasks.remove(requestDTO.id());
                        log.info("Notify user for Post ID: {}", requestDTO.id());
                    } catch (Exception e) {
                        log.error("Error executing scheduled task for post {}: {}", requestDTO.id(), e.getMessage());
                    }
                };

                //scheduled the notification task
                ScheduledFuture<?> scheduleTask =
                        taskScheduler.schedule(task, scheduleTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                tasks.put(requestDTO.id(), scheduleTask);
            }
        }
    }

    public void cancelScheduledPost(Long postId) {
        ScheduledFuture<?> future = tasks.remove(postId);
        if (future != null) {
            future.cancel(false);
            log.info("Cancelled task for Post {}", postId);
        }
    }

    @PostConstruct
    public void recoverScheduledPosts() {
        List<GeneratedContent> scheduledPosts = generatedContentRepository.findByIsScheduledTrue();
        LocalDateTime now = LocalDateTime.now();
        for (GeneratedContent post : scheduledPosts) {
            if (post.getScheduledAt().isAfter(now)) {
                ScheduleRequestDTO requestDTO = new ScheduleRequestDTO(post.getId(), post.getScheduledAt().toString());
                schedulePostNotification(requestDTO);
            } else {

                log.debug("Missed scheduled time for Post ID {}, notifying immediately...", post.getId());
                try {
                    //notify
                    log.debug("Notify user for Post ID: {}", post.getId());
                } catch (Exception e) {
                    log.error("Error notifying missed post {}: {}", post.getId(), e.getMessage());
                }
                post.setIsScheduled(false);
                generatedContentRepository.save(post);
            }
        }
    }

    @PostConstruct
    public void validateTaskMap() {
        log.info("Validating in-memory scheduled tasks...");

        if (tasks.isEmpty()) {
            log.info("No tasks found in the in-memory map.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        tasks.forEach((postId, future) -> {
            if (future.isCancelled() || future.isDone()) {
                log.warn("Task for Post ID {} is no longer active. Removing from map.", postId);
                tasks.remove(postId);
            } else {
                // fetch the post from DB for verification
                generatedContentRepository.findById(postId).ifPresentOrElse(post -> {
                    if (!Boolean.TRUE.equals(post.getIsScheduled()) || post.getScheduledAt().isBefore(now)) {
                        log.warn("Post ID {} is invalid in map (expired or unscheduled). Cancelling...", postId);
                        cancelScheduledPost(postId);
                    } else {
                        log.info("Task for Post ID {} is valid and scheduled at {}", postId, post.getScheduledAt());
                    }
                }, () -> {
                    log.warn("Post ID {} not found in DB. Cancelling...", postId);
                    cancelScheduledPost(postId);
                });
            }
        });
    }


}
