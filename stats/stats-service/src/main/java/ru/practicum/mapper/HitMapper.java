package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.AddHitRequestDto;
import ru.practicum.dto.StatsResponseDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class HitMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static Hit toHit(AddHitRequestDto endpointHit) {
        return Hit.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(LocalDateTime.parse(endpointHit.getTimestamp(), FORMATTER))
                .build();
    }

    public static StatsResponseDto toEndpointHit(Hit hit) {
        return StatsResponseDto.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .build();
    }
}
