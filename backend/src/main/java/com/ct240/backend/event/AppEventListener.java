package com.ct240.backend.event;

import com.ct240.backend.mapper.NotificationMapper;
import com.ct240.backend.service.SpaceUserService;
import com.ct240.backend.service.SseEmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class AppEventListener {
    @Autowired
    private SseEmitterService sseEmitterService;
    @Autowired
    private SpaceUserService spaceUserService;

    @Autowired
    NotificationMapper notificationMapper;

    @EventListener
    public void handleNotification(AppEvents.NotificationEvent event){
        sseEmitterService.sendToUser(event.notification().getUser().getId(), notificationMapper.toResponse(event.notification()));
    }

    @EventListener
    public void handleSseResponse(AppEvents.SpaceUpdateEvent event){
        List<String> userIds = spaceUserService.getAllUserIdsInSpace(event.sseResponse().getSpaceId());

        sseEmitterService.sendToUsers(userIds, event.sseResponse());
    }
}
