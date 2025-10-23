package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.user.dto.UserDto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class EventDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        @NotNull
        private Float lat;

        @NotNull
        private Float lon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewEventDto {
        @NotBlank
        @Size(min = 20, max = 2000)
        private String annotation;

        @NotNull
        private Long category;

        @NotBlank
        @Size(min = 20, max = 7000)
        private String description;

        @NotNull
        @Future
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventDate;

        @NotNull
        private Location location;

        private Boolean paid = false;

        @PositiveOrZero
        private Integer participantLimit = 0;

        private Boolean requestModeration = true;

        @NotBlank
        @Size(min = 3, max = 120)
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventShortDto {
        private Long id;
        private String annotation;
        private CategoryDto.CategoryResponse category;
        private Long confirmedRequests;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventDate;
        private UserDto.UserShortDto initiator;
        private Boolean paid;
        private String title;
        private Long views;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventFullDto {
        private Long id;
        private String annotation;
        private CategoryDto.CategoryResponse category;
        private Long confirmedRequests;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdOn;
        private String description;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventDate;
        private UserDto.UserShortDto initiator;
        private Location location;
        private Boolean paid;
        private Integer participantLimit;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime publishedOn;
        private Boolean requestModeration;
        private String state;
        private String title;
        private Long views;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateEventUserRequest {
        @Size(min = 20, max = 2000)
        private String annotation;

        private Long category;

        @Size(min = 20, max = 7000)
        private String description;

        @Future
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventDate;

        private Location location;

        private Boolean paid;

        @PositiveOrZero
        private Integer participantLimit;

        private Boolean requestModeration;

        private String stateAction; // SEND_TO_REVIEW, CANCEL_REVIEW

        @Size(min = 3, max = 120)
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateEventAdminRequest {
        @Size(min = 20, max = 2000)
        private String annotation;

        private Long category;

        @Size(min = 20, max = 7000)
        private String description;

        @Future
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventDate;

        private Location location;

        private Boolean paid;

        @PositiveOrZero
        private Integer participantLimit;

        private Boolean requestModeration;

        private String stateAction; // PUBLISH_EVENT, REJECT_EVENT

        @Size(min = 3, max = 120)
        private String title;
    }
}