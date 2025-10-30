package ru.practicum.ewm.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.Comment.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    List<Comment> findByAuthorId(Long authorId, Pageable pageable);

    List<Comment> findByAuthorIdAndEventId(Long authorId, Long eventId);

    Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);

    List<Comment> findByStatus(CommentStatus status, Pageable pageable);

    List<Comment> findByEventInitiatorIdAndEventId(Long initiatorId, Long eventId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE " +
            "(:users IS NULL OR c.author.id IN :users) AND " +
            "(:events IS NULL OR c.event.id IN :events) AND " +
            "(:statuses IS NULL OR c.status IN :statuses) AND " +
            "(:rangeStart IS NULL OR c.createdDate >= :rangeStart) AND " +
            "(:rangeEnd IS NULL OR c.createdDate <= :rangeEnd)")
    List<Comment> findCommentsByAdmin(@Param("users") List<Long> users,
                                      @Param("events") List<Long> events,
                                      @Param("statuses") List<CommentStatus> statuses,
                                      @Param("rangeStart") LocalDateTime rangeStart,
                                      @Param("rangeEnd") LocalDateTime rangeEnd,
                                      Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.event.id = :eventId AND c.status = 'PUBLISHED'")
    Long countPublishedCommentsByEventId(@Param("eventId") Long eventId);

    boolean existsByAuthorIdAndEventId(Long authorId, Long eventId);

    @Query("SELECT c.event.id, COUNT(c) FROM Comment c WHERE c.event.id IN :eventIds AND c.status = 'PUBLISHED' GROUP BY c.event.id")
    List<Object[]> countPublishedCommentsByEventIds(@Param("eventIds") List<Long> eventIds);
}