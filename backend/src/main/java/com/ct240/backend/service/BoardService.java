package com.ct240.backend.service;

import com.ct240.backend.dto.request.BoardCreationRequest;
import com.ct240.backend.dto.request.BoardUpdateRequest;
import com.ct240.backend.dto.response.BoardResponse;
import com.ct240.backend.entity.*;
import com.ct240.backend.enums.Role;
import com.ct240.backend.enums.Type;
import com.ct240.backend.exception.AppException;
import com.ct240.backend.exception.ErrorCode;
import com.ct240.backend.mapper.BoardMapper;
import com.ct240.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardService {
    @Autowired
    BoardRepository boardRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BoardUserRepository boardUserRepository;

    @Autowired
    SpaceRepository spaceRepository;

    @Autowired
    SpaceUserRepository spaceUserRepository;

    @Autowired
    BoardMapper boardMapper;

    @Autowired
    PermissionService permissionService;

    @Autowired
    NotificationService notificationService;

    public BoardResponse createBoard(String spaceId, BoardCreationRequest request, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        Space space = spaceRepository.findById(spaceId).orElseThrow(
                () -> new AppException(ErrorCode.SPACE_NOT_FOUND));

        boolean isMember = spaceUserRepository.existsByUserIdAndSpaceId(user.getId(), spaceId);
        if (!isMember)
        {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Board board = boardMapper.toBoard(request);
        board.setSpace(space);// relationship to space, that's why add this code
        board.setCreateAt(new Date());
        boardRepository.save(board);

        //tao BoardUserId
        BoardUserId boardUserId = new BoardUserId();
        boardUserId.setBoardId(board.getId());
        boardUserId.setUserId(user.getId());

        //tao BoardUser
        BoardUser boardUser = new BoardUser();
        boardUser.setId(boardUserId);
        boardUser.setBoard(board);
        boardUser.setUser(user);
        boardUser.setOwner(true);

        boardUserRepository.save(boardUser);

        return boardMapper.toBoardResponse(board);
    }
    public BoardResponse getBoard (String boardId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        //1.ADMIN & OWNER của SPACE coi được bất kì
        //2.MEMBER coi được cái BOARD công khai và các BOARD riêng tư nếu là thành viên

//        boolean isPrivate = boardRepository.isPrivate(boardId);
//        boolean isMember = boardUserRepository.existsByUserIdAndBoardId(user.getId(), boardId);
//        //Role roleAuthInSpace = permissionService.getRoleInSpaceByBoardId(user.getId(), boardId);
//
//        if (!isMember && isPrivate){
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }

        permissionService.requireInBoard(user.getId(), boardId);

        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new AppException(ErrorCode.BOARD_NOT_FOUND)
        );
//        // xuly private
//        if(board.isPrivate()){
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }

        return boardMapper.toBoardResponse(board);
        // muon dung Mapper o day phai code ben BoardMapper
    }

    public List<BoardResponse> getAllBoards(String spaceId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        boolean isMember = spaceUserRepository.existsByUserIdAndSpaceId(user.getId(), spaceId);
        if (!isMember){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // xuly private
        ///1. OWNER và ADMIN của SPACE coi được hết BOARD
        ///2. MEMBER chỉ coi được Board công khai
        ///3. Thành viên của Board mới coi được Board riêng tư

        List<Board> boardList;
        //chỉ hiện những board công khai
        //else
        Role role = permissionService.getRoleInSpace(user.getId(), spaceId);
        if(role == Role.OWNER || role == Role.ADMIN){
            boardList = boardRepository.findBySpaceId(spaceId);
        }
        else{
            //lấy tất cả những Board công khai trước
            boardList = boardRepository.findBySpaceIdAndIsPrivateFalse(spaceId);

            //lấy tất cả những Board riêng tư mà là thành viên Board
            boardList.addAll(boardRepository.findBySpaceIdAndIsPrivateTrue(spaceId, user.getId()));
            // boardList +=  boardRepository.findBySpaceIdAndIsPrivateTrue(spaceId, userId)
        }


        return boardList.stream()
                .map(board -> boardMapper.toBoardResponse(board))
                .collect(Collectors.toList());
    }


    public BoardResponse updateBoard( String boardId, BoardUpdateRequest request, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);
        //tim board
        Board board = boardRepository.findById(boardId).orElseThrow(
                () -> new AppException(ErrorCode.BOARD_NOT_FOUND)
        );
        //Lay spaceId tu board
        String spaceId = board.getSpace().getId();

        //Kiem duyet owner
        boolean isOwner = spaceUserRepository
                .existsByUserIdAndSpaceIdAndRole(user.getId(), spaceId, Role.OWNER);

        if (!isOwner){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        boardMapper.updateBoard(board, request);
        boardRepository.save(board);
        return boardMapper.toBoardResponse(board);

    }

        public void deleteBoard (String boardId, Authentication authentication){
            User user = permissionService.getUserAuth(authentication);

            Board board = boardRepository.findById(boardId).orElseThrow(
                    () -> new AppException(ErrorCode.BOARD_NOT_FOUND)
            );
            boolean isOwner = boardUserRepository.existsByUserIdAndBoardIdAndIsOwner(user.getId(), boardId, true);
            if (!isOwner){
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            List<User> users = boardUserRepository.findUsersByBoardId(boardId);
            notificationService.createNotificationForUsers(
                    users,
                    board.getName() + "đã bị xoá",
                    Type.DELETE_BOARD,
                    boardId
            );

            boardRepository.delete(board);
        }

}
