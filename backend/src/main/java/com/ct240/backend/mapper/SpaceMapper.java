package com.ct240.backend.mapper;

import com.ct240.backend.dto.request.SpaceCreationRequest;
import com.ct240.backend.dto.request.SpaceUpdateRequest;
import com.ct240.backend.dto.response.SpaceResponse;
import com.ct240.backend.entity.Space;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SpaceMapper {
    Space toSpace(SpaceCreationRequest request);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSpace(@MappingTarget  Space space, SpaceUpdateRequest request);

    SpaceResponse toSpaceResponse(Space space);

}
