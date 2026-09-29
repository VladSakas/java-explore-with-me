package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

@Component
public class CategoryMapper {

    public Category toEntity(NewCategoryDto request) {
        return Category.builder()
                .name(request.getName())
                .build();
    }

    public CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}