package ru.practicum.ewm.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventInitiatorCommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @GetMapping
    public List<CommentDto.CommentResponse> getEventComments(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Getting comments for event {} by initiator {}", eventId, userId);
        List<Comment> comments = commentService.getEventCommentsByInitiator(userId, eventId, from, size);
        return commentMapper.toResponseList(comments);
    }

    @DeleteMapping("/{commentId}")
    public void deleteCommentByInitiator(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable Long commentId) {

        log.info("Initiator {} deleting comment {} for event {}", userId, commentId, eventId);
        commentService.deleteCommentByEventInitiator(userId, eventId, commentId);
    }
}