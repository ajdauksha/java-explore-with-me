package ru.practicum.ewm.category.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.model.Category;

@UtilityClass
public class CategoryMapper {

    public static Category toEntity(CategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public static CategoryDto toResponse(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}