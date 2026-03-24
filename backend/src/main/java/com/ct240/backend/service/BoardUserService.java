package com.ct240.backend.service;

import com.ct240.backend.dto.request.BoardUserRequest;
import com.ct240.backend.dto.response.BoardMemberResponse;
import com.ct240.backend.dto.response.BoardUserResponse;
import com.ct240.backend.dto.response.UserResponse;
import com.ct240.backend.entity.Board;
import com.ct240.backend.entity.BoardUser;
import com.ct240.backend.entity.BoardUserId;
import com.ct240.backend.entity.User;
import com.ct240.backend.enums.Role;
import com.ct240.backend.enums.Type;
import com.ct240.backend.exception.AppException;
import com.ct240.backend.exception.ErrorCode;
import com.ct240.backend.mapper.BoardUserMapper;
import com.ct240.backend.mapper.UserMapper;
import com.ct240.backend.repository.BoardRepository;
import com.ct240.backend.repository.BoardUserRepository;
import com.ct240.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardUserService {
    @Autowired
    BoardUserRepository boardUserRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PermissionService permissionService;

    @Autowired
    BoardUserMapper boardUserMapper;
    @Autowired
    UserMapper userMapper;

    @Autowired
    NotificationService notificationService;

    ///         NGƯỜI CÓ QUYỀN THÊM MEMBER VÀO BOARD       ///
    /// 1. OWNER, ADMIN của SPACE                          ///
    /// 2. OWNER của BOARD                                 ///
    /// 3. MEMBER của SPACE nếu BOARD có isPrivate = FALSE ///
    /// *. Người được thêm chỉ là MEMBER của SPACE         ///
    public BoardUserResponse addMember(String boardId, BoardUserRequest request, Authentication authentication){
        User currentUser = permissionService.getUserAuth(authentication);

        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new AppException(ErrorCode.BOARD_NOT_FOUND)
        );

        User addedUser = userRepository.findById(request.getUserId()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        // - CHECK 1 - //
        Role role = permissionService.getRoleInSpaceByBoardId(currentUser.getId(), boardId);
        // - CHECK 2 - //
        boolean isBoardOwner = permissionService.isOwnerOfBoard(currentUser.getId(), boardId);
        // - CHECK 3 - //
        boolean isPrivate = boardRepository.isPrivate(boardId);

        if(! (
                role == Role.OWNER || role == Role.ADMIN // - CHECK 1 - //
                || isBoardOwner // - CHECK 2 - //
                || (role == Role.MEMBER && !isPrivate))){ // - CHECK 3 - //
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        //nếu người đó trong board rồi thì cũng hông được
        boolean isMember = permissionService.isMemberInBoard(request.getUserId(), boardId);
        if(isMember){
            throw new AppException(ErrorCode.USER_EXISTED_IN_BOARD);
        }

        //khởi tạo ID
        BoardUserId boardUserId = new BoardUserId();
        boardUserId.setUserId(addedUser.getId());
        boardUserId.setBoardId(boardId);

        //lưu
        BoardUser boardUser = new BoardUser();
        boardUser.setId(boardUserId);
        boardUser.setUser(addedUser);
        boardUser.setBoard(board);
        boardUser.setOwner(false);  // - CHECK * -  KHÔNG KHÔNG ĐƯỢC THAY ĐỔI

        notificationService.createNotification(
                addedUser,
                "Bạn được đã thêm vào bảng " + board.getName() ,
                Type.ADD_USER_IN_BOARD,
                boardId
        );

        boardUserRepository.save(boardUser);

        return boardUserMapper.toSpaceUserResponse(boardUser);
    }

    public List<BoardMemberResponse> getAllUsersInBoard(String boardId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        //kiểm tra phải thành viên board hông
        boolean isMember = permissionService.isMemberInBoard(user.getId(), boardId);
        if(!isMember){
            throw new AppException(ErrorCode.USER_NOT_EXIST_IN_BOARD);
        }

        var listUsers = boardUserRepository.findUsersByBoardId(boardId);


        return listUsers.stream()
                .map(u -> {
                    BoardMemberResponse boardMemberResponse = new BoardMemberResponse();
                    boardMemberResponse.setUserResponse(userMapper.toUserResponse(u));
                    boardMemberResponse.setOwner(permissionService.isOwnerOfBoard(u.getId(), boardId));
                    return  boardMemberResponse;
                })
                .collect(Collectors.toList());
    }

    //người khác xoá ra khỏi board
    ///         NGƯỜI CÓ QUYỀN XOÁ MEMBER KHỎI BOARD       ///
    /// 1. OWNER, ADMIN của SPACE                          ///
    /// 2. OWNER của BOARD                                 ///
    public void deleteUserFromBoard(String boardId, String userId, Authentication authentication){
        User currentUser = permissionService.getUserAuth(authentication);

        // - CHECK 1 - //
        Role role = permissionService.getRoleInSpaceByBoardId(currentUser.getId(), boardId);
        // - CHECK 2 - //
        boolean isBoardOwner = permissionService.isOwnerOfBoard(currentUser.getId(), boardId);
        if(! (
                role == Role.OWNER || role == Role.ADMIN // - CHECK 1 - //
                        || isBoardOwner ))// - CHECK 2 - //
            throw new AppException(ErrorCode.UNAUTHORIZED);

        //cái này sẽ check luôn có trong board đó hay không
        BoardUser boardUser = boardUserRepository.findByUserIdAndBoardId(userId, boardId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST_IN_BOARD)
        );

        User deletedUser = userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        notificationService.createNotification(
                deletedUser,
                "Bạn được đã bị xoá khỏi bảng " + boardUser.getBoard().getName() ,
                Type.ADD_USER_IN_BOARD,
                boardId
        );

        boardUserRepository.delete(boardUser);

    }

    //tự rời
    /// OWNER của BOARD hông được tự rời khỏi BOARD
    public void deleteUserFromBoard(String boardId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        boolean isOwner = permissionService.isOwnerOfBoard(user.getId(), boardId);
        if(isOwner){
            throw new AppException(ErrorCode.OWNER_CANNOT_LEAVE_BOARD);
        }

        BoardUser boardUser = boardUserRepository.findByUserIdAndBoardId(user.getId(), boardId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST_IN_BOARD)
        );

        boardUserRepository.delete(boardUser);

    }
}
