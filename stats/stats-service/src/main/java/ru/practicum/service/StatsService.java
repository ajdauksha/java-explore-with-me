package ru.practicum.service;

import ru.practicum.dto.AddHitRequestDto;
import ru.practicum.dto.StatsResponseDto;

import java.util.List;

public interface StatsService {
    void saveHit(AddHitRequestDto hitRequestDto);

    List<StatsResponseDto> getStats(String start, String end, List<String> uris, Boolean unique);
}
