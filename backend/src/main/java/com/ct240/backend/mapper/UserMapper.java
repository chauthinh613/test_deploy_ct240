package com.ct240.backend.mapper;

import com.ct240.backend.dto.request.UserCreationRequest;
import com.ct240.backend.dto.request.UserUpdateRequest;
import com.ct240.backend.dto.response.UserResponse;
import com.ct240.backend.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    @Mapping(target = "avatarURL", source = "avatarURL")
    UserResponse toUserResponse(User user);
}
