package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
@Slf4j
public class EventRequestsController {

    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto.ParticipationRequestResponse> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("User {}: getting requests for event {}", userId, eventId);
        return RequestMapper.toResponseList(requestService.getEventRequests(userId, eventId));
    }

    @PatchMapping
    public ParticipationRequestDto.EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody ParticipationRequestDto.EventRequestStatusUpdateRequest updateRequest) {

        log.info("User {}: updating request statuses for event {}", userId, eventId);

        Map<String, List<ru.practicum.ewm.request.model.ParticipationRequest>> result =
                requestService.updateRequestStatuses(userId, eventId, updateRequest.getRequestIds(),
                        ru.practicum.ewm.request.model.ParticipationRequest.RequestStatus.valueOf(updateRequest.getStatus()));

        return RequestMapper.toUpdateResult(
                result.get("confirmedRequests"),
                result.get("rejectedRequests")
        );
    }
}