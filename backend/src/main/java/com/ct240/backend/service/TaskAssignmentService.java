package com.ct240.backend.service;

import com.ct240.backend.dto.request.TaskAssignmentRequest;
import com.ct240.backend.dto.response.TaskAssignmentResponse;
import com.ct240.backend.dto.response.UserResponse;
import com.ct240.backend.entity.*;
import com.ct240.backend.enums.Role;
import com.ct240.backend.enums.Type;
import com.ct240.backend.exception.AppException;
import com.ct240.backend.exception.ErrorCode;
import com.ct240.backend.mapper.TaskAssignmentMapper;
import com.ct240.backend.mapper.UserMapper;
import com.ct240.backend.repository.BoardRepository;
import com.ct240.backend.repository.TaskAssignmentRepository;
import com.ct240.backend.repository.TaskRepository;
import com.ct240.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskAssignmentService {
    @Autowired
    TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    PermissionService permissionService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    TaskAssignmentMapper taskAssignmentMapper;

    @Autowired
    UserMapper userMapper;

    public TaskAssignmentResponse assignTask(String taskId, TaskAssignmentRequest request, Authentication authentication){
        // người mà chọn người khác làm
        User currentUser = permissionService.getUserAuth(authentication);

        User addedUser = userRepository.findById(request.getUserId()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        boolean addedUserIsMemberOfBoard = permissionService.isMemberInBoardByTaskId(addedUser.getId(), taskId);
        if (!addedUserIsMemberOfBoard){
            throw  new AppException(ErrorCode.USER_NOT_EXIST_IN_BOARD);
        }

        //người thực thi phải có trong board thì mới được?
        boolean currentUserIsMemberOfBoard = permissionService.isMemberInBoardByTaskId(currentUser.getId(), taskId);

        //lấy role người thực thi

        Role role = permissionService.getRoleInSpaceByTaskId(currentUser.getId(), taskId);

//        if(!currentUserIsMemberOfBoard){
//            throw new AppException(ErrorCode.USER_NOT_EXIST_IN_BOARD);
//        }

        //người được phân công cho người khác//
        //1. OWNER với ADMIN của Space
        //2. Nếu Board riêng tư thì chỉ thành viên trong Board
        //3. Nếu Board công khai thì cũng là thành viên trong Board
        //2-3: Chỉ thành viên trong Board mới giao task cho nhau (ngoại lệ là OWNER với ADMIN của SPACE)

        boolean canAssign = role == Role.ADMIN || role == Role.OWNER || currentUserIsMemberOfBoard;

        if(!canAssign){
            throw  new AppException(ErrorCode.UNAUTHORIZED);
        }

        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new AppException(ErrorCode.TASK_NOT_FOUND)
        );

        //tạo Id
        TaskAssignmentId taskAssignmentId = new TaskAssignmentId();
        taskAssignmentId.setUserId(request.getUserId());
        taskAssignmentId.setTaskId(taskId);

        TaskAssignment taskAssignment = new TaskAssignment();
        taskAssignment.setId(taskAssignmentId);
        taskAssignment.setTask(task);
        taskAssignment.setUser(addedUser);

        taskAssignmentRepository.save(taskAssignment);

        /// -- thêm hiển thị thông báo -- ///
        if(!addedUser.getId().equals(currentUser.getId())) { //nếu khác thì mới tạo thông báo
            notificationService.createNotification(
                    addedUser,
                    "Bạn được giao task mới",
                    Type.TASK_ASSIGNMENT,
                    taskId
            );
        }

        return TaskAssignmentResponse.builder()
                .taskId(taskId)
                .userId(request.getUserId())
                .user(userMapper.toUserResponse(addedUser))
                .build();
    }

    public List<UserResponse> getAllUsersInTask(String taskId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        ///Người xem được ai làm công việc///
        ///1. Nếu Board công khai thì ai cũng được miễn là thành viên của Space
        ///2. Nếu Board riêng tư thì (đảm bảo vẫn trong Space)
        ///                         2.1. OWNER với ADMIN của SPACE
        ///                         2.2. OWNER của BOARD (giống 2.3)
        ///                         2.3. Thành viên trong Board

        Board board = taskRepository.findBoardByTaskId(taskId);
        boolean isMemberOfBoard = permissionService.isMemberInBoardByTaskId(user.getId(), taskId);

        /// cái này đã kiểm tra là thành viên của Space hay không
        Role roleInSpace = permissionService.getRoleInSpaceByTaskId(user.getId(), taskId);

        boolean isPrivateBoard = board.isPrivate();

        if(isPrivateBoard){
            boolean canGetUsers = roleInSpace == Role.OWNER || roleInSpace == Role.ADMIN || isMemberOfBoard;

            if(!canGetUsers){
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        var listUsers = taskAssignmentRepository.findAllUsersByTaskId(taskId);

        return listUsers.stream()
                .map(u -> userMapper.toUserResponse(u))
                .collect(Collectors.toList());
    }

    public void unassignTask(String taskId, String userId, Authentication authentication){

        ///Người được xoá là ai/// -- Tương tự với AssignTask
        ///1. OWNER với ADMIN của Space
        ///2. Nếu Board riêng tư thì chỉ thành viên trong Board
        ///3. Nếu Board công khai thì cũng là thành viên trong Board
        ///2-3: Chỉ thành viên trong Board mới giao task cho nhau (ngoại lệ là OWNER với ADMIN của SPACE)

        User currentUser = permissionService.getUserAuth(authentication);

        //người thực thi phải có trong board thì mới được?
        boolean currentUserIsMemberOfBoard = permissionService.isMemberInBoardByTaskId(currentUser.getId(), taskId);

        //lấy role người thực thi
        Role role = permissionService.getRoleInSpaceByTaskId(currentUser.getId(), taskId);

        boolean canUnassigned = role == Role.ADMIN || role == Role.OWNER || currentUserIsMemberOfBoard;

        if(!canUnassigned){
            throw  new AppException(ErrorCode.UNAUTHORIZED);
        }

        TaskAssignment taskAssignment = taskAssignmentRepository.findByTaskIdAndUserId(taskId, userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_ASSIGNED_TO_TASK)
        );

        taskAssignmentRepository.delete(taskAssignment);
    }

    public void unassignAllTasksInBoard(String userId, String boardId){
        List<TaskAssignment> list = taskAssignmentRepository.findAllByUserIdAndBoardId(userId, boardId);

        taskAssignmentRepository.deleteAll(list);
    }

    public void unassignAllTasksInSpace(String userId, String spaceId){
        List<TaskAssignment> list = taskAssignmentRepository.findAllByUserIdAndSpaceId(userId, spaceId);

        taskAssignmentRepository.deleteAll(list);
    }
}
