package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateEventController {

    private final EventService eventService;
    private final ParticipationRequestRepository requestRepository;

    @GetMapping
    public List<EventDto.EventShortDto> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("User {}: getting events", userId);
        List<Event> events = eventService.getUserEvents(userId, from, size);

        return events.stream()
                .map(event -> {
                    Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(event.getId());
                    return EventMapper.toShortDto(event, confirmedRequests, 0L); // views будет из сервиса статистики
                })
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto.EventFullDto createEvent(
            @PathVariable Long userId,
            @Valid @RequestBody EventDto.NewEventDto eventDto) {

        log.info("User {}: creating event", userId);
        Event event = EventMapper.toEntity(eventDto);
        Event created = eventService.createEvent(userId, event);
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(created.getId());

        return EventMapper.toFullDto(created, confirmedRequests, 0L); // views будет из сервиса статистики
    }

    @GetMapping("/{eventId}")
    public EventDto.EventFullDto getUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {

        log.info("User {}: getting event {}", userId, eventId);
        Event event = eventService.getUserEvent(userId, eventId);
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);

        return EventMapper.toFullDto(event, confirmedRequests, 0L); // views будет из сервиса статистики
    }

    @PatchMapping("/{eventId}")
    public EventDto.EventFullDto updateEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventDto.UpdateEventUserRequest updateRequest) {

        log.info("User {}: updating event {}", userId, eventId);

        Event eventUpdate = new Event();
        EventMapper.updateEventFromUserRequest(updateRequest, eventUpdate);

        Event updatedEvent = eventService.updateEventByUser(userId, eventId, eventUpdate);
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);

        return EventMapper.toFullDto(updatedEvent, confirmedRequests, 0L);
    }
}