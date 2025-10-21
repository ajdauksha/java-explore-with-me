package ru.practicum.mapper;

import ru.practicum.dto.AddHitRequestDto;
import ru.practicum.dto.StatsResponseDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HitMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
