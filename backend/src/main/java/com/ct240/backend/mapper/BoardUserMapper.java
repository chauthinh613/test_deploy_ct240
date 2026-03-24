package com.ct240.backend.mapper;


import com.ct240.backend.dto.response.BoardUserResponse;
import com.ct240.backend.dto.response.SpaceUserResponse;
import com.ct240.backend.entity.BoardUser;
import com.ct240.backend.entity.SpaceUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface BoardUserMapper {
    @Mapping(source = "id.boardId", target = "boardId")
    @Mapping(source = "id.userId", target = "userId")
    BoardUserResponse toSpaceUserResponse(BoardUser boardUser);
}
