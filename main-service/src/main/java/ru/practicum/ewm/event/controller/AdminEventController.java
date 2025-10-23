package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminEventController {

    private final EventService eventService;
    private final ParticipationRequestRepository requestRepository;

    @GetMapping
    public List<EventDto.EventFullDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Admin: getting events with filters");

        List<Event.EventState> eventStates = null;
        if (states != null) {
            eventStates = states.stream()
                    .map(Event.EventState::valueOf)
                    .collect(Collectors.toList());
        }

        List<Event> events = eventService.getEventsByAdmin(users, eventStates, categories, rangeStart, rangeEnd, from, size);

        return events.stream()
                .map(event -> {
                    Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(event.getId());
                    return EventMapper.toFullDto(event, confirmedRequests, 0L); // views будет из сервиса статистики
                })
                .collect(Collectors.toList());
    }

    @PatchMapping("/{eventId}")
    public EventDto.EventFullDto updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventDto.UpdateEventAdminRequest updateRequest) {

        log.info("Admin: updating event with id: {}", eventId);

        Event eventUpdate = new Event();
        EventMapper.updateEventFromAdminRequest(updateRequest, eventUpdate);

        Event updatedEvent = eventService.updateEventByAdmin(eventId, eventUpdate);
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);

        return EventMapper.toFullDto(updatedEvent, confirmedRequests, 0L); // views будет из сервиса статистики
    }
}