package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.dto.EventDto;

import java.util.ArrayList;
import java.util.List;

public class CompilationDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewCompilationDto {
        private List<Long> events = new ArrayList<>();
        private Boolean pinned = false;

        @NotBlank
        @Size(min = 1, max = 50)
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCompilationRequest {
        private List<Long> events;
        private Boolean pinned;

        @Size(min = 1, max = 50)
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompilationResponse {
        private Long id;
        private List<EventDto.EventShortDto> events;
        private Boolean pinned;
        private String title;
    }
}