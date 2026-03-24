package com.ct240.backend.mapper;

import com.ct240.backend.dto.request.CardCreationRequest;
import com.ct240.backend.dto.request.CardUpdateRequest;
import com.ct240.backend.dto.request.MoveCardRequest;
import com.ct240.backend.dto.response.CardResponse;
import com.ct240.backend.entity.Card;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CardMapper {
    Card toCard(CardCreationRequest request);
    @Mapping(source = "board.id", target ="boardId")
    CardResponse toCardResponse (Card card);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCard (@MappingTarget Card card, CardUpdateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCard (@MappingTarget Card card, MoveCardRequest request);
}
