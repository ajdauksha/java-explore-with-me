package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateCommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto.CommentResponse createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody CommentDto.NewCommentDto commentDto) {

        log.info("User {} creating comment for event {}", userId, eventId);
        Comment comment = commentMapper.toEntity(commentDto);
        Comment created = commentService.createComment(userId, eventId, comment);
        return commentMapper.toResponse(created);
    }

    @PatchMapping("/{commentId}")
    public CommentDto.CommentResponse updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDto.UpdateCommentDto commentDto) {

        log.info("User {} updating comment {}", userId, commentId);
        Comment commentUpdate = new Comment();
        commentUpdate.setText(commentDto.getText());
        Comment updated = commentService.updateComment(userId, commentId, commentUpdate);
        return commentMapper.toResponse(updated);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAuthor(
            @PathVariable Long userId,
            @PathVariable Long commentId) {

        log.info("User {} deleting comment {}", userId, commentId);
        commentService.deleteCommentByAuthor(userId, commentId);
    }

    @GetMapping
    public List<CommentDto.CommentResponse> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Getting comments by user {}", userId);
        List<Comment> comments = commentService.getUserComments(userId, from, size);
        return commentMapper.toResponseList(comments);
    }
}