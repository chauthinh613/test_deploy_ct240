package com.ct240.backend.controller;

import com.ct240.backend.dto.request.CardCreationRequest;
import com.ct240.backend.dto.request.CardUpdateRequest;
import com.ct240.backend.dto.request.MoveCardRequest;
import com.ct240.backend.dto.response.ApiResponse;
import com.ct240.backend.dto.response.CardResponse;
import com.ct240.backend.service.CardService;
import jakarta.validation.Valid;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.AbstractPastInstantBasedValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CardController {
    @Autowired
    CardService cardService;

    @PostMapping ("/boards/{boardId}/cards")
    public ApiResponse<CardResponse> createCard(
            @PathVariable String boardId,
            @RequestBody @Valid CardCreationRequest request,
            Authentication authentication){
        ApiResponse<CardResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(cardService.createCard(boardId, request, authentication));
        return apiResponse;
    }
    @GetMapping ("/boards/{boardId}/cards")
    public ApiResponse<List<CardResponse>> getAllCards(
            @PathVariable String boardId,
            Authentication authentication){
        ApiResponse<List<CardResponse>> apiResponse = new ApiResponse<>();

        apiResponse.setData(cardService.getAllCards(boardId, authentication));
        return apiResponse;
    }
    @PutMapping ("/cards/{cardId}")
    public ApiResponse<CardResponse> updateCard(
            @PathVariable String cardId,
            @RequestBody @Valid CardUpdateRequest request,
            Authentication authentication){
        ApiResponse<CardResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(cardService.updateCard(cardId, request, authentication));

        return apiResponse;
    }

    @PutMapping("/cards/{cardId}/move")
    public ApiResponse<CardResponse> moveCard(
            @PathVariable String cardId,
            @RequestBody @Valid MoveCardRequest request,
            Authentication authentication){
        ApiResponse<CardResponse> apiResponse = new ApiResponse<>();

        apiResponse.setData(cardService.moveCard(cardId, request, authentication));

        return apiResponse;
    }

    @DeleteMapping ("/cards/{cardId}")
    public ApiResponse<Void> deleteCard(@PathVariable String cardId, Authentication authentication){
        ApiResponse<Void> apiResponse = new ApiResponse<>();

        cardService.deleteCard(cardId, authentication);
        apiResponse.setMessage("Card and its Tasks are deleted successfully");

        return apiResponse;
    }



}
