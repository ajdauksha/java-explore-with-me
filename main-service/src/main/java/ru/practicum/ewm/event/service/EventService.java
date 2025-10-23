package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Event.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.DataConflictException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    @Transactional
    public Event createEvent(Long userId, Event event) {
        log.info("Creating event for user: {}", userId);

        User initiator = userService.getUserById(userId);
        event.setInitiator(initiator);

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DataConflictException("Event date must be at least 2 hours from now");
        }

        return eventRepository.save(event);
    }

    public List<Event> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Getting events for user: {}, from: {}, size: {}", userId, from, size);

        userService.getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findByInitiatorId(userId, pageable);
    }

    public Event getUserEvent(Long userId, Long eventId) {
        log.info("Getting event: {} for user: {}", eventId, userId);

        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + eventId + " for user: " + userId));
    }

    @Transactional
    public Event updateEventByUser(Long userId, Long eventId, Event updatedEvent) {
        log.info("Updating event: {} by user: {}", eventId, userId);

        Event existingEvent = getUserEvent(userId, eventId);

        if (existingEvent.getState() != EventState.PENDING && existingEvent.getState() != EventState.CANCELED) {
            throw new DataConflictException("Only pending or canceled events can be changed");
        }

        if (updatedEvent.getEventDate() != null &&
                updatedEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DataConflictException("Event date must be at least 2 hours from now");
        }

        updateEventFields(existingEvent, updatedEvent);

        return eventRepository.save(existingEvent);
    }

    public List<Event> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Integer from, Integer size) {
        log.info("Getting events by admin with filters");

        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findEventsByAdmin(users, states, categories, rangeStart, rangeEnd, pageable);
    }

    @Transactional
    public Event updateEventByAdmin(Long eventId, Event updatedEvent) {
        log.info("Updating event: {} by admin", eventId);

        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + eventId));

        if (updatedEvent.getStateAction() != null) {
            handleAdminStateAction(existingEvent, updatedEvent.getStateAction());
        }

        updateEventFields(existingEvent, updatedEvent);
        return eventRepository.save(existingEvent);
    }

    public List<Event> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                       LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                       Boolean onlyAvailable, String sort, Integer from, Integer size) {
        log.info("Getting public events with filters");

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        Pageable pageable = createPublicPageable(sort, from, size);
        return eventRepository.findPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable);
    }

    public Event getPublicEvent(Long eventId) {
        log.info("Getting public event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with id: " + eventId));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NoSuchElementException("Event is not published");
        }

        return event;
    }

    private void updateEventFields(Event existing, Event updated) {
        if (updated.getAnnotation() != null) existing.setAnnotation(updated.getAnnotation());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getEventDate() != null) existing.setEventDate(updated.getEventDate());
        if (updated.getLocationLat() != null) existing.setLocationLat(updated.getLocationLat());
        if (updated.getLocationLon() != null) existing.setLocationLon(updated.getLocationLon());
        if (updated.getPaid() != null) existing.setPaid(updated.getPaid());
        if (updated.getParticipantLimit() != null) existing.setParticipantLimit(updated.getParticipantLimit());
        if (updated.getRequestModeration() != null) existing.setRequestModeration(updated.getRequestModeration());
        if (updated.getState() != null) existing.setState(updated.getState());

        if (updated.getCategory() != null) {
            existing.setCategory(updated.getCategory());
        }
    }

    private void handleAdminStateAction(Event event, StateAction stateAction) {
        switch (stateAction) {
            case PUBLISH_EVENT:
                if (event.getState() != EventState.PENDING) {
                    throw new DataConflictException("Only pending events can be published");
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new DataConflictException("Event date must be at least 1 hour from publication");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case REJECT_EVENT:
                if (event.getState() == EventState.PUBLISHED) {
                    throw new DataConflictException("Published events cannot be rejected");
                }
                event.setState(EventState.CANCELED);
                break;
        }
    }

    private Pageable createPublicPageable(String sort, Integer from, Integer size) {
        if ("EVENT_DATE".equals(sort)) {
            return PageRequest.of(from / size, size, Sort.by("eventDate").descending());
        } else if ("VIEWS".equals(sort)) {
            return PageRequest.of(from / size, size, Sort.by("views").descending());
        } else {
            return PageRequest.of(from / size, size);
        }
    }

    public enum StateAction {
        PUBLISH_EVENT, REJECT_EVENT, SEND_TO_REVIEW, CANCEL_REVIEW
    }
}