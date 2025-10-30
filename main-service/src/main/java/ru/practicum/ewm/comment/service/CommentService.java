package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.Comment.CommentStatus;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Event.EventState;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;

    private static final int EDIT_WINDOW_HOURS = 24;

    @Transactional
    public Comment createComment(Long userId, Long eventId, Comment comment) {
        log.info("Creating comment by user {} for event {}", userId, eventId);

        userService.getUserById(userId);
        Event event = eventService.getPublicEvent(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new IllegalStateException("Cannot comment on unpublished event");
        }

        if (commentRepository.existsByAuthorIdAndEventId(userId, eventId)) {
            throw new IllegalStateException("User has already commented on this event");
        }

        comment.setAuthor(userService.getUserById(userId));
        comment.setEvent(event);
        comment.setStatus(CommentStatus.PENDING);

        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(Long userId, Long commentId, Comment updatedComment) {
        log.info("Updating comment {} by user {}", commentId, userId);

        Comment existingComment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found or user is not the author"));

        validateCommentEditable(existingComment);

        existingComment.setText(updatedComment.getText());
        existingComment.setStatus(CommentStatus.PENDING);
        existingComment.setUpdatedDate(LocalDateTime.now());

        return commentRepository.save(existingComment);
    }

    @Transactional
    public void deleteCommentByAuthor(Long userId, Long commentId) {
        log.info("Deleting comment {} by author {}", commentId, userId);

        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found or user is not the author"));

        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteCommentByEventInitiator(Long initiatorId, Long eventId, Long commentId) {
        log.info("Deleting comment {} by event initiator {}", commentId, initiatorId);

        eventService.getUserEvent(initiatorId, eventId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Comment does not belong to the specified event");
        }

        comment.setStatus(CommentStatus.DELETED);
        comment.setModerationReason("Deleted by event initiator");
        comment.setModeratedDate(LocalDateTime.now());
        commentRepository.save(comment);
    }

    @Transactional
    public Comment moderateComment(Long commentId, Comment moderation) {
        log.info("Moderating comment {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new IllegalStateException("Only pending comments can be moderated");
        }

        switch (moderation.getStatus()) {
            case PUBLISHED:
                comment.setStatus(CommentStatus.PUBLISHED);
                comment.setModerationReason(moderation.getModerationReason());
                break;
            case REJECTED:
                comment.setStatus(CommentStatus.REJECTED);
                comment.setModerationReason(moderation.getModerationReason());
                break;
            default:
                throw new IllegalArgumentException("Invalid moderation action");
        }

        comment.setModeratedDate(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public List<Comment> getEventComments(Long eventId, Integer from, Integer size) {
        log.info("Getting published comments for event {}", eventId);

        Pageable pageable = PageRequest.of(from / size, size);
        return commentRepository.findByEventIdAndStatus(eventId, CommentStatus.PUBLISHED, pageable);
    }

    public List<Comment> getUserComments(Long userId, Integer from, Integer size) {
        log.info("Getting comments by user {}", userId);

        userService.getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        return commentRepository.findByAuthorId(userId, pageable);
    }

    public List<Comment> getCommentsForModeration(Integer from, Integer size) {
        log.info("Getting comments for moderation");

        Pageable pageable = PageRequest.of(from / size, size);
        return commentRepository.findByStatus(CommentStatus.PENDING, pageable);
    }

    public List<Comment> getEventCommentsByInitiator(Long initiatorId, Long eventId, Integer from, Integer size) {
        log.info("Getting comments for event {} by initiator {}", eventId, initiatorId);

        eventService.getUserEvent(initiatorId, eventId);

        Pageable pageable = PageRequest.of(from / size, size);
        return commentRepository.findByEventInitiatorIdAndEventId(initiatorId, eventId, pageable);
    }

    public List<Comment> getCommentsByAdmin(List<Long> users, List<Long> events, List<String> statuses,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                            Integer from, Integer size) {
        log.info("Getting comments by admin with filters");

        List<CommentStatus> commentStatuses = null;
        if (statuses != null && !statuses.isEmpty()) {
            commentStatuses = statuses.stream()
                    .map(String::toUpperCase)
                    .map(CommentStatus::valueOf)
                    .toList();
        }

        Pageable pageable = PageRequest.of(from / size, size);
        return commentRepository.findCommentsByAdmin(users, events, commentStatuses, rangeStart, rangeEnd, pageable);
    }

    private void validateCommentEditable(Comment comment) {
        LocalDateTime editDeadline = comment.getCreatedDate().plusHours(EDIT_WINDOW_HOURS);

        if (LocalDateTime.now().isAfter(editDeadline)) {
            throw new IllegalStateException("Comment can only be edited within " + EDIT_WINDOW_HOURS + " hours of creation");
        }

        if (comment.getStatus() == CommentStatus.DELETED || comment.getStatus() == CommentStatus.REJECTED) {
            throw new IllegalStateException("Cannot edit deleted or rejected comment");
        }
    }

    public Long getPublishedCommentsCountByEventId(Long eventId) {
        return commentRepository.countPublishedCommentsByEventId(eventId);
    }
}