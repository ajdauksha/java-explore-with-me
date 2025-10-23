package ru.practicum.ewm.category.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto.CategoryResponse> getCategories(
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Public: getting categories from: {}, size: {}", from, size);
        return categoryService.getCategories(from, size).stream()
                .map(CategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{catId}")
    public CategoryDto.CategoryResponse getCategory(@PathVariable Long catId) {
        log.info("Public: getting category with id: {}", catId);
        return CategoryMapper.toResponse(categoryService.getCategoryById(catId));
    }
}