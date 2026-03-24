package com.ct240.backend.mapper;


import com.ct240.backend.dto.request.SpaceUserUpdateRequest;
import com.ct240.backend.dto.response.SpaceUserResponse;
import com.ct240.backend.entity.SpaceUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SpaceUserMapper {

    @Mapping(source = "id.spaceId", target = "spaceId")
    @Mapping(source = "id.userId", target = "userId")
    SpaceUserResponse toSpaceUserResponse(SpaceUser spaceUser);


    void updateSpaceUser(@MappingTarget SpaceUser spaceUser, SpaceUserUpdateRequest request);
}
