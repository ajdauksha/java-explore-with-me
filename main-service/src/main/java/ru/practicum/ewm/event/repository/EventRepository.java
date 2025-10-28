package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    @Query("SELECT e FROM Event e WHERE " +
            "(:users IS NULL OR e.initiator.id IN :users) AND " +
            "(:states IS NULL OR e.state IN :states) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(cast(:rangeStart AS DATE) IS NULL OR e.eventDate >= :rangeStart) AND " +
            "(cast(:rangeEnd AS DATE) IS NULL OR e.eventDate <= :rangeEnd)")
    List<Event> findEventsByAdmin(@Param("users") List<Long> users,
                                  @Param("states") List<Event.EventState> states,
                                  @Param("categories") List<Long> categories,
                                  @Param("rangeStart") LocalDateTime rangeStart,
                                  @Param("rangeEnd") LocalDateTime rangeEnd,
                                  Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
            "e.state = 'PUBLISHED' AND " +
            "(cast(:text AS STRING) IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', cast(:text AS STRING), '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', cast(:text AS STRING), '%'))) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(:paid IS NULL OR e.paid = :paid) AND " +
            "(cast(:rangeStart AS DATE) IS NULL OR e.eventDate >= :rangeStart) AND " +
            "(cast(:rangeEnd AS DATE) IS NULL OR e.eventDate <= :rangeEnd) AND " +
            "(:onlyAvailable IS NULL OR :onlyAvailable = false OR " +
            "(e.participantLimit = 0 OR e.participantLimit > (" +
            "SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event = e AND r.status = 'CONFIRMED')))")
    List<Event> findPublicEvents(@Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 @Param("onlyAvailable") Boolean onlyAvailable,
                                 Pageable pageable);

    Boolean existsByCategoryId(Long categoryId);

    List<Event> findByIdIn(List<Long> eventIds);

}