package ru.practicum.service.publicapi;

import ru.practicum.dto.category.CategoryDto;

import java.util.List;

public interface PublicCategoryService {

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long catId);
}