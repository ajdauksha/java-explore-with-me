package ru.practicum.ewm.comment.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    public Comment toEntity(CommentDto.NewCommentDto dto) {
        return Comment.builder()
                .text(dto.getText())
                .build();
    }

    public CommentDto.CommentResponse toResponse(Comment comment) {
        return CommentDto.CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(UserMapper.toShortDto(comment.getAuthor()))
                .eventId(comment.getEvent().getId())
                .status(comment.getStatus().name())
                .createdDate(comment.getCreatedDate())
                .updatedDate(comment.getUpdatedDate())
                .moderatedDate(comment.getModeratedDate())
                .moderationReason(comment.getModerationReason())
                .build();
    }

    public CommentDto.CommentWithEventResponse toResponseWithEvent(Comment comment) {
        return CommentDto.CommentWithEventResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(UserMapper.toShortDto(comment.getAuthor()))
                .event(EventMapper.toShortDto(comment.getEvent(), 0L, 0L))
                .status(comment.getStatus().name())
                .createdDate(comment.getCreatedDate())
                .updatedDate(comment.getUpdatedDate())
                .build();
    }

    public List<CommentDto.CommentResponse> toResponseList(List<Comment> comments) {
        return comments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CommentDto.CommentWithEventResponse> toResponseWithEventList(List<Comment> comments) {
        return comments.stream()
                .map(this::toResponseWithEvent)
                .collect(Collectors.toList());
    }
}