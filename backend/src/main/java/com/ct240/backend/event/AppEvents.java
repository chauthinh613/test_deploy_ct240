package com.ct240.backend.event;

import com.ct240.backend.dto.response.SseResponse;
import com.ct240.backend.entity.Notification;
import com.ct240.backend.entity.User;
import com.ct240.backend.enums.Type;

import java.util.List;


public class AppEvents {
    // Event dùng để báo có thông báo mới (Notification)
    public record NotificationEvent(
            Notification notification
    ) {}

    // Event dùng để báo có thay đổi chung trong Space (SseResponse)
    public record SpaceUpdateEvent(
            SseResponse sseResponse
    ) {}
}
