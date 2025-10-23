package ru.practicum.ewm.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto.CategoryResponse createCategory(@Valid @RequestBody CategoryDto.CategoryRequest categoryDto) {
        log.info("Admin: creating category {}", categoryDto.getName());
        Category category = CategoryMapper.toEntity(categoryDto);
        Category created = categoryService.createCategory(category);
        return CategoryMapper.toResponse(created);
    }

    @PatchMapping("/{catId}")
    public CategoryDto.CategoryResponse updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto.CategoryRequest categoryDto) {

        log.info("Admin: updating category with id: {}", catId);
        Category category = CategoryMapper.toEntity(new CategoryDto.CategoryRequest(categoryDto.getName()));
        category.setId(catId);
        Category updated = categoryService.updateCategory(catId, category);
        return CategoryMapper.toResponse(updated);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Admin: deleting category with id: {}", catId);
        categoryService.deleteCategory(catId);
    }
}