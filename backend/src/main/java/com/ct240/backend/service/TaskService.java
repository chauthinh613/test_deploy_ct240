package com.ct240.backend.service;

import com.ct240.backend.dto.request.CompleteTaskRequest;
import com.ct240.backend.dto.request.MoveTaskRequest;
import com.ct240.backend.dto.request.TaskCreationRequest;
import com.ct240.backend.dto.request.TaskUpdateRequest;
import com.ct240.backend.dto.response.CardResponse;
import com.ct240.backend.dto.response.SseResponse;
import com.ct240.backend.dto.response.TaskResponse;
import com.ct240.backend.entity.Board;
import com.ct240.backend.entity.Card;
import com.ct240.backend.entity.Task;
import com.ct240.backend.entity.User;
import com.ct240.backend.enums.Type;
import com.ct240.backend.event.AppEvents;
import com.ct240.backend.exception.AppException;
import com.ct240.backend.exception.ErrorCode;
import com.ct240.backend.mapper.TaskMapper;
import com.ct240.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    TaskRepository taskRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    CardRepository cardRepository;

    @Autowired
    SpaceUserRepository spaceUserRepository;

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    PermissionService permissionService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    public TaskResponse createTask (String cardId, TaskCreationRequest request, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new AppException(ErrorCode.CARD_NOT_FOUND)
        );

        Board board = boardRepository.findById(card.getBoard().getId()).orElseThrow(
                () -> new AppException(ErrorCode.BOARD_NOT_FOUND)
        );

        //coi có quyền dưới board hay hông
        permissionService.requireInBoard(user.getId(), board.getId());

        //lấy position có vị cao nhất
        Integer maxPosition = taskRepository.findMaxPositionByCardId(cardId);
        int newPosition = (maxPosition == null) ? 0 : maxPosition + 1000; //lỡ mới tạo là chưa có maxPosition
        //Tao task
        Task task = taskMapper.toTask(request);
        task.setCreateAt(new Date());
        task.setPosition(newPosition);
        task.setCard(card);

        taskRepository.save(task);

        eventPublisher.publishEvent(new AppEvents.SpaceUpdateEvent(
                SseResponse.builder()
                        .type(Type.CARD_TASK_UPDATE)
                        .spaceId(board.getSpace().getId())
                        .build())
        );

        return taskMapper.toTaskResponse(task);

    }

    public List<TaskResponse> getAllTasks(String cardId, Authentication authentication) {
        User user = permissionService.getUserAuth(authentication);

        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new AppException(ErrorCode.CARD_NOT_FOUND)
        );

        Board board = boardRepository.findById(card.getBoard().getId()).orElseThrow(
                () -> new AppException(ErrorCode.BOARD_NOT_FOUND)
        );


//        boolean isMember = spaceUserRepository.existsByUserIdAndSpaceId(user.getId(), spaceId);
//        if (!isMember){
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }

        //coi có quyền dưới board hay hông
        permissionService.requireInBoard(user.getId(), board.getId());

        var taskList = taskRepository.findByCardId(cardId);

        return taskList.stream()
                .map(task -> taskMapper.toTaskResponse(task))
                .sorted(Comparator.comparing(TaskResponse::getPosition))
                .collect(Collectors.toList());
    }

    public TaskResponse updateTask (String taskId, TaskUpdateRequest request,  Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new AppException(ErrorCode.TASK_NOT_FOUND)
        );

        Card card = cardRepository.findById(task.getCard().getId()).orElseThrow(
                () -> new AppException(ErrorCode.CARD_NOT_FOUND)
        );

        String spaceId = task.getCard().getBoard().getSpace().getId();

        Board board = boardRepository.findById(card.getBoard().getId()).orElseThrow(
                () -> new AppException(ErrorCode.BOARD_NOT_FOUND)
        );

        //coi có quyền dưới board hay hông
        permissionService.requireInBoard(user.getId(), board.getId());

//        boolean isMember = spaceUserRepository.existsByUserIdAndSpaceId(user.getId(), spaceId);
//        if (!isMember){
//            throw new AppException(ErrorCode.UNAUTHORIZED);
//        }
        // update task
        taskMapper.updateTask(task, request);
        taskRepository.save(task);

        eventPublisher.publishEvent(new AppEvents.SpaceUpdateEvent(
                SseResponse.builder()
                        .type(Type.CARD_TASK_UPDATE)
                        .spaceId(board.getSpace().getId())
                        .build())
        );

        return taskMapper.toTaskResponse(task);
    }

    public TaskResponse moveTask(String taskId, MoveTaskRequest request, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new AppException(ErrorCode.TASK_NOT_FOUND)
        );

        Card card = cardRepository.findById(task.getCard().getId()).orElseThrow(
                () -> new AppException(ErrorCode.CARD_NOT_FOUND)
        );

        Board board = boardRepository.findById(card.getBoard().getId()).orElseThrow(
                () -> new AppException(ErrorCode.BOARD_NOT_FOUND)
        );

        //coi có quyền dưới board hay hông
        permissionService.requireInBoard(user.getId(), board.getId());

        Card moveToCard = cardRepository.findById(request.getCardId()).orElseThrow(
                () -> new AppException(ErrorCode.CARD_NOT_FOUND)
        );
        taskMapper.updateTask(task, request);
        task.setCard(moveToCard);

        taskRepository.save(task);

        eventPublisher.publishEvent(new AppEvents.SpaceUpdateEvent(
                SseResponse.builder()
                        .type(Type.CARD_TASK_UPDATE)
                        .spaceId(board.getSpace().getId())
                        .build())
        );

        return taskMapper.toTaskResponse(task);

    }

    public TaskResponse complete(String taskId, CompleteTaskRequest request, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new AppException(ErrorCode.TASK_NOT_FOUND)
        );

        Card card = cardRepository.findById(task.getCard().getId()).orElseThrow(
                () -> new AppException(ErrorCode.CARD_NOT_FOUND)
        );

        Board board = boardRepository.findById(card.getBoard().getId()).orElseThrow(
                () -> new AppException(ErrorCode.BOARD_NOT_FOUND)
        );

        //coi có quyền dưới board hay hông
        permissionService.requireInBoard(user.getId(), board.getId());

        taskMapper.updateTask(task, request);

        taskRepository.save(task);

        eventPublisher.publishEvent(new AppEvents.SpaceUpdateEvent(
                SseResponse.builder()
                        .type(Type.CARD_TASK_UPDATE)
                        .spaceId(board.getSpace().getId())
                        .build())
        );

        return taskMapper.toTaskResponse(task);

    }

    public void deleteTask(String taskId, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new AppException(ErrorCode.TASK_NOT_FOUND)
        );

        String spaceId = task.getCard().getBoard().getSpace().getId();

        boolean isMember = spaceUserRepository.existsByUserIdAndSpaceId(user.getId(), spaceId);
        if (!isMember){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        taskRepository.delete(task);

        eventPublisher.publishEvent(new AppEvents.SpaceUpdateEvent(
                SseResponse.builder()
                        .type(Type.CARD_TASK_UPDATE)
                        .spaceId(spaceId)
                        .build())
        );
    }
}
