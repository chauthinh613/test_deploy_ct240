package com.ct240.backend.mapper;

import com.ct240.backend.dto.response.NotificationResponse;
import com.ct240.backend.entity.Notification;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")

public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public void updateNotification(@MappingTarget Notification notification, Boolean readStatus);
}
