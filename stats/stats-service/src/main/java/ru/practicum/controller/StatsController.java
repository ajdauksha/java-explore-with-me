package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.AddHitRequestDto;
import ru.practicum.dto.StatsResponseDto;
import ru.practicum.service.StatsService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique) {

        log.info("Getting stats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        return statsService.getStats(URLDecoder.decode(start, StandardCharsets.UTF_8), URLDecoder.decode(end, StandardCharsets.UTF_8), uris, unique);
    }
}
