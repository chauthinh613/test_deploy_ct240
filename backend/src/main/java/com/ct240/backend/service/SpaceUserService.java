package com.ct240.backend.service;

import com.ct240.backend.dto.request.SpaceUserRequest;
import com.ct240.backend.dto.request.SpaceUserUpdateRequest;
import com.ct240.backend.dto.response.SpaceMemberResponse;
import com.ct240.backend.dto.response.SpaceUserResponse;
import com.ct240.backend.dto.response.SseResponse;
import com.ct240.backend.entity.*;
import com.ct240.backend.enums.Role;
import com.ct240.backend.enums.Type;
import com.ct240.backend.event.AppEvents;
import com.ct240.backend.exception.AppException;
import com.ct240.backend.exception.ErrorCode;
import com.ct240.backend.mapper.SpaceUserMapper;
import com.ct240.backend.mapper.UserMapper;
import com.ct240.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpaceUserService {
    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    SpaceUserRepository spaceUserRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SpaceUserMapper spaceUserMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    PermissionService permissionService;

    @Autowired
    BoardUserService boardUserService;

    @Autowired
    TaskAssignmentService taskAssignmentService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    NotificationService notificationService;


    public SpaceUserResponse addMember(String spaceId, SpaceUserRequest request, Authentication authentication){
//        String username = authentication.getName();
          //lấy người đang thực hiện công việc này là ai để kiểm tra quyền
//        User currentUser = userRepository.findByUsername(username).orElseThrow(
//                () -> new AppException(ErrorCode.USER_NOT_FOUND)
//        );
        //Minh thay doan tren bang dong nay
        User currentUser = permissionService.getUserAuth(authentication);

        Space space = spaceRepository.findById(spaceId).orElseThrow(
                () -> new AppException(ErrorCode.SPACE_NOT_FOUND)
        );  //----> đã được kiểm tra ở bước kiểm tra role sau

        /// chỉ có owner với admin mới thêm được người
        permissionService.requireSpaceAdmin(currentUser.getId(), spaceId);

        /// hướng xử lý nếu người dùng đã ở trong đây rồi
        /// 1. xử lý là nếu có thì thông báo đã tồn tại --> đang làm
        /// 2. xử lý là nếu thay đổi thì chạy qua updateRole?

        User addedUser = userRepository.findById(request.getUserId()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        boolean isMember = permissionService.isMemberInSpace(addedUser.getId(), spaceId);
        if(isMember){ /// người này đã tồn tại trong space rồi
            throw  new AppException(ErrorCode.USER_EXISTED_IN_SPACE);
        }

        //tạo SpaceUserId
        SpaceUserId spaceUserId = new SpaceUserId();
        spaceUserId.setSpaceId(spaceId);
        spaceUserId.setUserId(addedUser.getId());

        //lưu
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setId(spaceUserId);
        spaceUser.setSpace(space);
        spaceUser.setUser(addedUser);
        /// không cho set role là OWNER (mặc dù trên front end cũng hông có hiện để mà chọn)
        if(request.getRole() == Role.OWNER){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        spaceUser.setRole(request.getRole());


        notificationService.createNotification(
                addedUser,
                "Bạn được đã thêm vào space " + space.getName(),
                Type.ADD_USER_IN_SPACE,
                spaceId
        );

        eventPublisher.publishEvent(new AppEvents.SpaceUpdateEvent(
                SseResponse.builder()
                        .type(Type.SPACE_MEMBER_UPDATE)
                        .spaceId(spaceId)
                        .build())
        );
        //sseEmitterService.createSseResponse(spaceId, Type.SPACE_MEMBER_UPDATE);

        spaceUserRepository.save(spaceUser);

        return spaceUserMapper.toSpaceUserResponse(spaceUser);
    }

    public List<SpaceMemberResponse> getAllMembersInSpace(String spaceId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        if(!spaceRepository.existsById(spaceId)) {
            throw new AppException(ErrorCode.SPACE_NOT_FOUND);
        }

        /// chỉ cần là thành viên của space là có thể coi danh sách thành viên
        boolean isMember = permissionService.isMemberInSpace(user.getId(), spaceId);
        if(!isMember){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        var listUsers = spaceUserRepository.findUsersBySpaceId(spaceId);

        return listUsers.stream()
                .map(u -> {
                    SpaceMemberResponse spaceMemberResponse = new SpaceMemberResponse();
                    spaceMemberResponse.setUserResponse(userMapper.toUserResponse(u));
                    spaceMemberResponse.setRole(permissionService.getRoleInSpace(u.getId(), spaceId));
                    return spaceMemberResponse;
                })
                .collect(Collectors.toList());
    }


    public List<String> getAllUserIdsInSpace(String spaceId){
        var listUsers = spaceUserRepository.findUsersBySpaceId(spaceId);
        return listUsers.stream().map(User::getId).collect(Collectors.toList());
    }

    public SpaceUserResponse updateRole(String spaceId, String userId, SpaceUserUpdateRequest request, Authentication authentication){
        User currentUser = permissionService.getUserAuth(authentication);

        permissionService.requireSpaceAdmin(currentUser.getId(), spaceId);

        SpaceUser spaceUser = spaceUserRepository.findByUserIdAndSpaceId(userId, spaceId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST_IN_SPACE)
        );
        // nếu nó đã là owner ròi thì không thay đổi được -> tránh mất owner của space
        if(spaceUser.getRole() == Role.OWNER){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        User changedRoleUser = userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        spaceUserMapper.updateSpaceUser(spaceUser, request);

        notificationService.createNotification(
                changedRoleUser,
                "Bạn đã được đổi vai trò trong space " + spaceUser.getSpace().getName() + " thành " + request.getRole(),
                Type.CHANGE_ROLE_SPACE,
                spaceId
        );

        eventPublisher.publishEvent(new AppEvents.SpaceUpdateEvent(
                SseResponse.builder()
                        .type(Type.SPACE_MEMBER_UPDATE)
                        .spaceId(spaceId)
                        .build())
        );

        spaceUserRepository.save(spaceUser);

        return spaceUserMapper.toSpaceUserResponse(spaceUser);
    }

    public void deleteUserFromSpace(String spaceId, String userId, Authentication authentication){
        User currentUser = permissionService.getUserAuth(authentication);
        User deletedUser = userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        //người thực hiện là OWNER với ADMIN thì được
//        Role roleAuth = permissionService.getRoleInSpace(currentUser.getId(), spaceId);
//        if(roleAuth != Role.OWNER && roleAuth != Role.ADMIN){
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
        permissionService.requireSpaceAdmin(currentUser.getId(), spaceId);
        /// --- nếu người được xoá là OWNER thì hông được --- ///
        Role roleDeleted = permissionService.getRoleInSpace(deletedUser.getId(), spaceId);
        if(roleDeleted == Role.OWNER){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        SpaceUser spaceUser = spaceUserRepository.findByUserIdAndSpaceId(userId, spaceId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST_IN_SPACE)
        );

        notificationService.createNotification(
                deletedUser,
                "Bạn đã bị xoá khỏi space " + spaceUser.getSpace().getName(),
                Type.DELETE_USER_FROM_SPACE,
                spaceId
        );

        spaceUserRepository.delete(spaceUser);

        //// --- /// ----- KHI XOÁ THÌ XOÁ LUÔN TRONG BOARD VA TASK ASSIGNMENT /// --- ///
        boardUserService.deleteUserFromAllBoards(userId, spaceId);
        taskAssignmentService.unassignAllTasksInSpace(userId, spaceId);

        eventPublisher.publishEvent(new AppEvents.SpaceUpdateEvent(
                SseResponse.builder()
                        .type(Type.SPACE_BOARD_UPDATE)
                        .spaceId(spaceId)
                        .build())
        );


    }

    ///authentication là người tự rời
    public void deleteUserFromSpace(String spaceId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        SpaceUser spaceUser = spaceUserRepository.findByUserIdAndSpaceId(user.getId(), spaceId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST_IN_SPACE)
        );
        /// --- NẾU LÀ OWNER THÌ KHÔNG ĐƯỢC TỰ OUT --- ///
        if(spaceUser.getRole() == Role.OWNER){
            throw new AppException(ErrorCode.OWNER_CANNOT_LEAVE_SPACE);
        }

        List<SpaceUser> ownerOfSpace = spaceUserRepository.findBySpaceIdAndRole(spaceId, Role.OWNER);
        List<SpaceUser> adminOfSpace = spaceUserRepository.findBySpaceIdAndRole(spaceId, Role.ADMIN);
        List<SpaceUser> list = new ArrayList<>();
        list.addAll(ownerOfSpace);
        list.addAll(adminOfSpace);

        notificationService.createNotificationForUsers(
                list.stream().map(SpaceUser::getUser).toList(),
                user.getUsername() + " đã rời khỏi space " + spaceUser.getSpace().getName() ,
                Type.DELETE_USER_FROM_SPACE,
                spaceId
        );

        eventPublisher.publishEvent(new AppEvents.SpaceUpdateEvent(
                SseResponse.builder()
                        .type(Type.SPACE_MEMBER_UPDATE)
                        .spaceId(spaceId)
                        .build())
        );

        spaceUserRepository.delete(spaceUser);

        //// --- /// ----- KHI XOÁ THÌ XOÁ LUÔN TRONG BOARD VA TASK ASSIGNMENT /// --- ///
        boardUserService.deleteUserFromAllBoards(user.getId(), spaceId);
        taskAssignmentService.unassignAllTasksInSpace(user.getId(), spaceId);

    }
}
