package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.AddHitRequestDto;
import ru.practicum.dto.StatsResponseDto;
import ru.practicum.service.StatsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@RequestBody AddHitRequestDto addHitRequestDto) {
        log.info("Add new hit: app={}, uri={}, ip={}, timestamp={}",
                addHitRequestDto.getApp(), addHitRequestDto.getUri(), addHitRequestDto.getIp(), addHitRequestDto.getTimestamp());

        statsService.saveHit(addHitRequestDto);
    }

    @GetMapping("/stats")
    public List<StatsResponseDto> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {

        log.info("Getting stats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        return statsService.getStats(start, end, uris, unique);
    }
}
