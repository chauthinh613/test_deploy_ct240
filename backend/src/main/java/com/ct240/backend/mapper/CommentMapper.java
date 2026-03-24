package com.ct240.backend.mapper;
import com.ct240.backend.dto.request.BoardCreationRequest;
import com.ct240.backend.dto.request.BoardUpdateRequest;
import com.ct240.backend.dto.request.CommentCreationRequest;
import com.ct240.backend.dto.response.BoardResponse;
import com.ct240.backend.dto.response.CommentResponse;
import com.ct240.backend.entity.Board;
import com.ct240.backend.entity.Comment;
import org.hibernate.query.results.internal.complete.CompleteFetchBuilderEntityValuedModelPart;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArray;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import javax.crypto.spec.PSource;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toComment (CommentCreationRequest request);

    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "createAt", target = "createAt")
    CommentResponse toCommentResponse (Comment comment);
}
