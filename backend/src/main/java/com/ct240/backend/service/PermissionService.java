package com.ct240.backend.service;


import com.ct240.backend.entity.*;
import com.ct240.backend.enums.Role;
import com.ct240.backend.exception.AppException;
import com.ct240.backend.exception.ErrorCode;
import com.ct240.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    SpaceUserRepository spaceUserRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    BoardUserRepository boardUserRepository;

    @Autowired
    TaskRepository taskRepository;



    // Lay User dang dang nhap
    public User getUserAuth(Authentication authentication) {
        String username = authentication.getName();

        return userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );
    }

    @Transactional(readOnly = true)
    public String getUserIdAuth(Authentication authentication){
        String username = authentication.getName();

        // Chỉ query id thôi, không load entity
        return userRepository.findIdByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );
    }

    public boolean isMemberInSpace(String userId, String spaceId) {
        return spaceUserRepository.existsByUserIdAndSpaceId(userId, spaceId);
    }

    public Role getRoleInSpace(String userId, String spaceId) {
        SpaceUser su = spaceUserRepository.findByUserIdAndSpaceId(userId, spaceId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST_IN_SPACE)
        );
        return su.getRole();
    }

    public Role getRoleInSpaceByBoardId(String userId, String boardId) {
        Space space = boardRepository.findSpaceByBoardId(boardId);

        return getRoleInSpace(userId, space.getId());
    }

    public boolean isOwnerOfBoard(String userId, String boardId) {
        BoardUser bu = boardUserRepository.findByUserIdAndBoardId(userId, boardId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST_IN_BOARD)
        );

        return bu.isOwner();
    }

    public boolean isMemberInBoard(String userId, String boardId) {
        return boardUserRepository.existsByUserIdAndBoardId(userId, boardId);
    }

    public Task getTask (String taskId){
        return taskRepository.findById(taskId).orElseThrow(
                () -> new AppException(ErrorCode.TASK_NOT_FOUND)
        );
    }

    public Task requireTaskMember(String userId, String taskId){
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new AppException(ErrorCode.TASK_NOT_FOUND)
        );
        String spaceId = task.getCard().getBoard().getSpace().getId();
        if (!isMemberInSpace(userId, spaceId)){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return task;

    }

    public void requireSpaceAdmin(String userId, String spaceId) {
        spaceRepository.findById(spaceId).orElseThrow(
                () -> new AppException(ErrorCode.SPACE_NOT_FOUND)
        );

        Role role = getRoleInSpace(userId, spaceId);
        if (role != Role.OWNER && role != Role.ADMIN)
            throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public void requireSpaceMember (String userId, String spaceId){
        if (!spaceUserRepository.existsByUserIdAndSpaceId(userId, spaceId))
            throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public void requireBoardMember (String userId, String boardId){
        if (!boardUserRepository.existsByUserIdAndBoardId(userId, boardId))
            throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public void requireBoardOwner (String userId, String boardId){
        BoardUser boardUser = boardUserRepository.findByUserIdAndBoardId(userId, boardId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.USER_EXISTED_IN_BOARD)
                );
        if (!boardUser.isOwner())
            throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    public boolean isMemberInBoardByTaskId(String userId, String taskId){
        Board board = taskRepository.findBoardByTaskId(taskId);

        return boardUserRepository.existsByUserIdAndBoardId(userId, board.getId());
    }

    public Role getRoleInSpaceByTaskId(String userId, String taskId){
        Board board = taskRepository.findBoardByTaskId(taskId);

        return getRoleInSpaceByBoardId(userId, board.getId());
    }

    public void requireInBoard(String userId, String boardId){
        boolean isPrivate = boardRepository.isPrivate(boardId);
        boolean isMemberInBoard = boardUserRepository.existsByUserIdAndBoardId(userId, boardId);
        Role roleInSpace = getRoleInSpaceByBoardId(userId, boardId);

        if (roleInSpace != Role.OWNER && roleInSpace != Role.ADMIN && !isMemberInBoard && isPrivate){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

}
