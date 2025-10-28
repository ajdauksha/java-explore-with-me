package ru.practicum.ewm.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto.CompilationResponse createCompilation(@Valid @RequestBody CompilationDto.NewCompilationDto compilationDto) {
        log.info("Admin: creating compilation {}", compilationDto.getTitle());
        Compilation compilation = CompilationMapper.toEntity(compilationDto);
        Compilation created = compilationService.createCompilation(compilation);
        return CompilationMapper.toResponse(created);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Admin: deleting compilation with id: {}", compId);
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto.CompilationResponse updateCompilation(
            @PathVariable Long compId,
            @Valid @RequestBody CompilationDto.UpdateCompilationRequest updateRequest) {

        log.info("Admin: updating compilation with id: {}", compId);

        Compilation updated = compilationService.updateCompilation(compId, CompilationMapper.toEntity(compId, updateRequest));
        return CompilationMapper.toResponse(updated);
    }
}