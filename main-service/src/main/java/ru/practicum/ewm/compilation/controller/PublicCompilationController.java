package ru.practicum.ewm.compilation.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.service.CompilationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto.CompilationResponse> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Public: getting compilations with pinned: {}", pinned);
        return compilationService.getCompilations(pinned, from, size).stream()
                .map(CompilationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{compId}")
    public CompilationDto.CompilationResponse getCompilation(@PathVariable Long compId) {
        log.info("Public: getting compilation with id: {}", compId);
        return CompilationMapper.toResponse(compilationService.getCompilationById(compId));
    }
}