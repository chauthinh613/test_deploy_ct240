package com.ct240.backend.service;

import com.ct240.backend.dto.request.CommentCreationRequest;
import com.ct240.backend.dto.response.CommentResponse;
import com.ct240.backend.entity.*;
import com.ct240.backend.enums.Type;
import com.ct240.backend.exception.AppException;
import com.ct240.backend.exception.ErrorCode;
import com.ct240.backend.mapper.CommentMapper;
import com.ct240.backend.repository.CommentRepository;
import com.ct240.backend.repository.TaskAssignmentRepository;
import com.ct240.backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    PermissionService permissionService;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    NotificationService notificationService;

    public CommentResponse createComment(
            String taskId, CommentCreationRequest request, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);
        Task task = permissionService.getTask(taskId);

        Comment comment = commentMapper.toComment(request);
        comment.setTask(task);
        comment.setUser(user);
        comment.setCreateAt(new Date());

        List<User> users = taskAssignmentRepository.findAllUsersByTaskId(taskId);
        notificationService.createNotificationForUsers(
                users,
                "Bạn có bình luận mới trong task được giao",
                Type.COMMENT,
                taskId
        );

        commentRepository.save(comment);
        return commentMapper.toCommentResponse(comment);
    }

    public List<CommentResponse> getAllComments (String taskId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);
        Task task = permissionService.requireTaskMember(user.getId(), taskId);

//        Card card = task.getCard();
//        Board board =  card.getBoard();
        String spaceID = task.getCard().getBoard().getSpace().getId();

        if (!permissionService.isMemberInSpace(user.getId(), spaceID)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        var commentList = commentRepository.findByTaskId(taskId);

        return commentList.stream()
                .map(comment ->commentMapper.toCommentResponse(comment))
                .collect(Collectors.toList());
    }
    public void deleteComment (String commentId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_FOUND)
        );

        if (!comment.getUser().getId().equals(user.getId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        commentRepository.delete(comment);
    }
}
