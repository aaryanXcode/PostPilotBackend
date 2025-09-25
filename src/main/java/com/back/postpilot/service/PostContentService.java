package com.back.postpilot.service;

import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.DTO.ScheduleRequestDTO;
import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import com.back.postpilot.EnumTypeConstants.ContentStatus;
import com.back.postpilot.cron.PostSchedulerService;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.repository.GeneratedContentRepository;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class PostContentService {

    private  final GeneratedContentRepository generatedContentRepository;
    private final PlatformContentPostServiceFactory platformContentPostServiceFactory;
    private final PostSchedulerService postSchedulerService;
    PostContentService(PlatformContentPostServiceFactory platformContentPostServiceFactory,
                       GeneratedContentRepository generatedContentRepository,
                       PostSchedulerService postSchedulerService){
        this.platformContentPostServiceFactory = platformContentPostServiceFactory;
        this.generatedContentRepository = generatedContentRepository;
        this.postSchedulerService = postSchedulerService;
    }

    public String postContent(GenerateContentDTO generateContentDTO) throws Exception {
        ContentPlatForms platform = ContentPlatForms.valueOf(generateContentDTO.platform().toUpperCase());
        PlatformContentPostService platformContentPostService = platformContentPostServiceFactory.getPlatformService(platform);
        String result =  platformContentPostService.postContent(generateContentDTO);
        if (generateContentDTO.id() != null) {
            GeneratedContent generatedContent = generatedContentRepository.findById(generateContentDTO.id())
                    .orElseThrow(() -> new RuntimeException("GeneratedContent not found"));

            generatedContent.setStatus(ContentStatus.PUBLISHED);
            generatedContentRepository.save(generatedContent);
        }
        return result;
    }

    public boolean setSchedulePost(ScheduleRequestDTO request){
        LocalDateTime scheduleTime = LocalDateTime.parse(request.dateTime());
        postSchedulerService.schedulePostNotification(request);
        int updated = generatedContentRepository.updateSchedule(request.id(), scheduleTime);
        return updated > 0;
    }

    public boolean cancelSchedulePost(Long postId){
        // Cancel the scheduled notification
        postSchedulerService.cancelScheduledPost(postId);
        
        // Set scheduledAt to null and isScheduled to false
        int updated = generatedContentRepository.cancelSchedule(postId);
        return updated > 0;
    }

}
