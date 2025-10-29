package ru.practicum.ewm.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public static Compilation toEntity(CompilationDto.NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .events(dto.getEvents().stream()
                        .map(eventId -> Event.builder().id(eventId).build())
                        .collect(Collectors.toSet()))
                .build();
    }

    public static Compilation toEntity(Long compId, CompilationDto.UpdateCompilationRequest updateRequest) {
        Compilation compilation = new Compilation();
        compilation.setId(compId);

        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }

        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>();
            for (Long eventId : updateRequest.getEvents()) {
                events.add(Event.builder().id(eventId).build());
            }
            compilation.setEvents(events);
        }

        return compilation;
    }

    public static CompilationDto.CompilationResponse toResponse(Compilation compilation) {
        return CompilationDto.CompilationResponse.builder()
                .id(compilation.getId())
                .events(compilation.getEvents().stream()
                        .map(event -> EventMapper.toShortDto(event, 0L, 0L))
                        .collect(Collectors.toList()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}