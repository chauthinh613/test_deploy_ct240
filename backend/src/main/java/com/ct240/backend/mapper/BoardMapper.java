package com.ct240.backend.mapper;

import com.ct240.backend.dto.request.BoardCreationRequest;
import com.ct240.backend.dto.request.BoardUpdateRequest;
import com.ct240.backend.dto.response.BoardResponse;
import com.ct240.backend.entity.Board;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArray;
import org.mapstruct.*;

import javax.crypto.spec.PSource;

@Mapper(componentModel = "spring")
public interface BoardMapper {
    Board toBoard(BoardCreationRequest request);
//    void updateBoard(@MappingTarget Board board, BoardUpdateRequest request);
    //spaceID : String, phai dung @Mapping de covert qua
    @Mapping(source = "space.id", target = "spaceId") //target + ignore (khong hien thi)
    @Mapping(source = "private", target = "isPrivate")
    BoardResponse toBoardResponse (Board board);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBoard(@MappingTarget Board board, BoardUpdateRequest request);


}
