package ru.practicum.ewm.event.controller;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
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

        // Логируем информацию для статистики
        log.info("Client IP: {}, Endpoint path: {}", request.getRemoteAddr(), request.getRequestURI());

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("End date must be after start date");
        }

        List<Event> events = eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        return events.stream()
                .map(event -> {
                    Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(event.getId());
                    return EventMapper.toShortDto(event, confirmedRequests, 0L); // views будет из сервиса статистики
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public EventDto.EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Public: getting event with id: {}", id);

        // Логируем информацию для статистики
        log.info("Client IP: {}, Endpoint path: {}", request.getRemoteAddr(), request.getRequestURI());

        Event event = eventService.getPublicEvent(id);
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(id);

        return EventMapper.toFullDto(event, confirmedRequests, 0L); // views будет из сервиса статистики
    }
}