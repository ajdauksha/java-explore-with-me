package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @GetMapping
    public List<CommentDto.CommentWithEventResponse> getComments(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> events,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Admin getting comments with filters");
        List<Comment> comments = commentService.getCommentsByAdmin(users, events, statuses, rangeStart, rangeEnd, from, size);
        return commentMapper.toResponseWithEventList(comments);
    }

    @GetMapping("/moderation")
    public List<CommentDto.CommentWithEventResponse> getCommentsForModeration(
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Admin getting comments for moderation");
        List<Comment> comments = commentService.getCommentsForModeration(from, size);
        return commentMapper.toResponseWithEventList(comments);
    }

    @PatchMapping("/{commentId}/moderate")
    public CommentDto.CommentWithEventResponse moderateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDto.CommentModerationDto moderationDto) {

        log.info("Admin moderating comment {}", commentId);
        Comment moderation = new Comment();
        moderation.setStatus(Comment.CommentStatus.valueOf(moderationDto.getAction()));
        moderation.setModerationReason(moderationDto.getModerationReason());

        Comment moderated = commentService.moderateComment(commentId, moderation);
        return commentMapper.toResponseWithEvent(moderated);
    }

    @DeleteMapping("/{commentId}")
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        log.info("Admin deleting comment {}", commentId);
        Comment moderation = new Comment();
        moderation.setStatus(Comment.CommentStatus.REJECTED);
        moderation.setModerationReason("Deleted by administrator");
        commentService.moderateComment(commentId, moderation);
    }
}