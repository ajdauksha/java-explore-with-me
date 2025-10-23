package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventInitiatorIdAndEventId(Long initiatorId, Long eventId);

    @Query("SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r FROM ParticipationRequest r WHERE r.id IN :requestIds AND r.status = 'PENDING'")
    List<ParticipationRequest> findPendingRequestsByIds(@Param("requestIds") List<Long> requestIds);

    Boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

}