package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.AddHitRequestDto;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicEventController {

    private final EventService eventService;
    private final ParticipationRequestRepository requestRepository;
    private final StatsClient statsClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public List<EventDto.EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request) {

        log.info("Public: getting events with text: {}, categories: {}", text, categories);

        recordEventView(request.getRemoteAddr());

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("End date must be after start date");
        }

        List<Event> events = eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        return events.stream()
                .map(event -> {
                    Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(event.getId());
                    return EventMapper.toShortDto(event, confirmedRequests, 0L);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public EventDto.EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Public: getting event with id: {}", id);

        recordEventView(id, request.getRemoteAddr());

        Event event = eventService.getPublicEvent(id);
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(id);

        long views = getViews(id, event);

        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    public void recordEventView(Long eventId, String userIp) {
        log.info("Client IP: {}, Endpoint path: {}", userIp, "/events/" + eventId);
        statsClient.saveHit(AddHitRequestDto.builder()
                .app("ewm-main-service")
                .uri("/events/" + eventId)
                .ip(userIp)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build());
    }

    public void recordEventView(String userIp) {
        log.info("Client IP: {}, Endpoint path: {}", userIp, "/events");
        statsClient.saveHit(AddHitRequestDto.builder()
                .app("ewm-main-service")
                .uri("/events")
                .ip(userIp)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build());
    }

    private int getViews(Long id, Event event) {
        return statsClient
                .getStats(event.getPublishedOn(), LocalDateTime.now(), List.of("/events/" + id), true)
                .size();
    }

}