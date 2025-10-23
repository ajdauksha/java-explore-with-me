package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Event.EventState;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.DataConflictException;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;

    @Transactional
    public ParticipationRequest createRequest(Long userId, Long eventId) {
        log.info("Creating participation request for user: {} to event: {}", userId, eventId);

        userService.getUserById(userId);

        Event event;
        try {
            event = eventService.getPublicEvent(eventId);
        } catch (NoSuchElementException e) {
            throw new DataConflictException("Event is not published " + eventId);
        }


        validateRequestCreation(userId, event);

        ParticipationRequest request = ParticipationRequest.builder()
                .requester(userService.getUserById(userId))
                .event(event)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationRequest.RequestStatus.CONFIRMED);
        }

        return requestRepository.save(request);
    }

    public List<ParticipationRequest> getUserRequests(Long userId) {
        log.info("Getting requests for user: {}", userId);

        userService.getUserById(userId);
        return requestRepository.findByRequesterId(userId);
    }

    @Transactional
    public ParticipationRequest cancelRequest(Long userId, Long requestId) {
        log.info("Canceling request: {} by user: {}", requestId, userId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Request not found with id: " + requestId));

        if (!request.getRequester().getId().equals(userId)) {
            throw new DataConflictException("User can only cancel their own requests");
        }

        request.setStatus(ParticipationRequest.RequestStatus.CANCELED);
        return requestRepository.save(request);
    }

    public List<ParticipationRequest> getEventRequests(Long userId, Long eventId) {
        log.info("Getting requests for event: {} by user: {}", eventId, userId);

        Event event = eventService.getUserEvent(userId, eventId);
        return requestRepository.findByEventInitiatorIdAndEventId(userId, eventId);
    }

    @Transactional
    public Map<String, List<ParticipationRequest>> updateRequestStatuses(
            Long userId, Long eventId, List<Long> requestIds, ParticipationRequest.RequestStatus newStatus) {

        log.info("Updating request statuses for event: {} by user: {}", eventId, userId);

        Event event = eventService.getUserEvent(userId, eventId);
        List<ParticipationRequest> requests = requestRepository.findPendingRequestsByIds(requestIds);

        if (requests.isEmpty()) {
            throw new DataConflictException("No pending requests found with provided ids");
        }

        if (newStatus == ParticipationRequest.RequestStatus.CONFIRMED) {
            return confirmRequests(event, requests);
        } else {
            return rejectRequests(requests);
        }
    }

    private Map<String, List<ParticipationRequest>> confirmRequests(Event event, List<ParticipationRequest> requests) {
        Long confirmedCount = requestRepository.countConfirmedRequestsByEventId(event.getId());
        int availableSlots = event.getParticipantLimit() - confirmedCount.intValue();

        if (availableSlots <= 0) {
            throw new DataConflictException("The participant limit has been reached");
        }

        List<ParticipationRequest> toConfirm = requests.stream()
                .limit(availableSlots)
                .collect(Collectors.toList());

        List<ParticipationRequest> toReject = requests.stream()
                .skip(availableSlots)
                .collect(Collectors.toList());

        toConfirm.forEach(req -> req.setStatus(ParticipationRequest.RequestStatus.CONFIRMED));
        toReject.forEach(req -> req.setStatus(ParticipationRequest.RequestStatus.REJECTED));

        requestRepository.saveAll(toConfirm);
        requestRepository.saveAll(toReject);

        return Map.of(
                "confirmedRequests", toConfirm,
                "rejectedRequests", toReject
        );
    }

    private Map<String, List<ParticipationRequest>> rejectRequests(List<ParticipationRequest> requests) {
        requests.forEach(req -> req.setStatus(ParticipationRequest.RequestStatus.REJECTED));
        requestRepository.saveAll(requests);

        return Map.of(
                "rejectedRequests", requests,
                "confirmedRequests", List.of()
        );
    }

    private void validateRequestCreation(Long userId, Event event) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new DataConflictException("Initiator cannot request participation in their own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new DataConflictException("Cannot participate in unpublished event");
        }

        if (requestRepository.existsByEventIdAndRequesterId(event.getId(), userId)) {
            throw new DataConflictException("Request already exists for this event and user");
        }

        Long confirmedCount = requestRepository.countConfirmedRequestsByEventId(event.getId());
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new DataConflictException("The participant limit has been reached");
        }
    }
}