package ru.practicum.ewm.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateRequestController {

    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto.ParticipationRequestResponse> getUserRequests(@PathVariable Long userId) {
        log.info("User {}: getting requests", userId);
        return RequestMapper.toResponseList(requestService.getUserRequests(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto.ParticipationRequestResponse createRequest(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId) {

        log.info("User {}: creating request for event {}", userId, eventId);
        return RequestMapper.toResponse(requestService.createRequest(userId, eventId));
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto.ParticipationRequestResponse cancelRequest(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {

        log.info("User {}: canceling request {}", userId, requestId);
        return RequestMapper.toResponse(requestService.cancelRequest(userId, requestId));
    }
}