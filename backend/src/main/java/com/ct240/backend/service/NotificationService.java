package com.ct240.backend.service;

import com.ct240.backend.dto.response.NotificationResponse;
import com.ct240.backend.entity.Notification;
import com.ct240.backend.entity.User;
import com.ct240.backend.enums.Type;
import com.ct240.backend.exception.AppException;
import com.ct240.backend.exception.ErrorCode;
import com.ct240.backend.mapper.NotificationMapper;
import com.ct240.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    NotificationMapper notificationMapper;

    @Autowired
    PermissionService permissionService;

    @Autowired
    SseEmitterService sseEmitterService;

    ///lấy danh sách thông báo
    public List<NotificationResponse> getAllNotifications(Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        var notificationList = notificationRepository.findByUserId(user.getId());

        return notificationList.stream()
                .map(n -> notificationMapper.toResponse(n))
                .collect(Collectors.toList());
    }

    public void createNotification(User user, String content, Type type, String referenceId){

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setContent(content);
        notification.setReadStatus(false);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setCreateAt(new Date());

        notificationRepository.save(notification);

        // push realtime
        sseEmitterService.sendToUser(user.getId(), notificationMapper.toResponse(notification));
    }

    public void createNotificationForUsers(List<User> users, String content, Type type, String referenceId){
        for (User user : users) {
            createNotification(user, content, type, referenceId);
        }
    }

    public void deleteNotification(String notificationId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND)
        );

        if(!notificationRepository.existsByIdAndUserId(notificationId, user.getId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        notificationRepository.delete(notification);
    }

    public void updateStatusRead(String notificationId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND)
        );

        if(!notificationRepository.existsByIdAndUserId(notificationId, user.getId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        notificationMapper.updateNotification(notification, true);
        notificationRepository.save(notification);
    }
}
