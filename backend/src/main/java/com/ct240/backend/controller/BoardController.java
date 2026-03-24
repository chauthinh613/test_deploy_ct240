package com.ct240.backend.controller;

import com.ct240.backend.dto.request.BoardCreationRequest;
import com.ct240.backend.dto.request.BoardUpdateRequest;
import com.ct240.backend.dto.response.ApiResponse;
import com.ct240.backend.dto.response.BoardResponse;
import com.ct240.backend.entity.BoardUser;
import com.ct240.backend.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import java.util.List;

@RestController
public class BoardController {
    @Autowired
    BoardService boardService;

    @PostMapping ("/spaces/{spaceId}/boards")
    ApiResponse<BoardResponse> createBoard(@PathVariable String spaceId, @RequestBody @Valid BoardCreationRequest request, Authentication authentication){
        ApiResponse<BoardResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(boardService.createBoard(spaceId, request, authentication));
        return apiResponse;
    }

    @GetMapping ("/boards/{boardId}")// cai nay
    ApiResponse<BoardResponse> getBoard(@PathVariable String boardId, Authentication authentication){
        ApiResponse<BoardResponse> apiResponse = new ApiResponse<>();

        BoardResponse boardResponse = boardService.getBoard(boardId, authentication);

        apiResponse.setData(boardResponse);

        return apiResponse;
    }

    @GetMapping ("/spaces/{spaceId}/boards")
    ApiResponse<List<BoardResponse>> getAllBoards (@PathVariable String spaceId, Authentication authentication){
        ApiResponse<List<BoardResponse>> apiResponse = new ApiResponse<>();

        apiResponse.setData(boardService.getAllBoards(spaceId, authentication));

        return apiResponse;
    }

    @PutMapping ("/boards/{boardId}")
    ApiResponse<BoardResponse> updateBoard(@PathVariable String boardId,
                                           @RequestBody @Valid BoardUpdateRequest request,
                                           Authentication authentication){
        ApiResponse<BoardResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(boardService.updateBoard(boardId, request, authentication));

        return apiResponse;
    }

    @DeleteMapping ("/boards/{boardId}")
    ApiResponse<Void> deleteBoard (@PathVariable String boardId, Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        boardService.deleteBoard(boardId, authentication);
        apiResponse.setMessage("Board is deleted successfully");

        return apiResponse;
    }


}
