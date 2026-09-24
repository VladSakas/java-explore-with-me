package ru.practicum.service.admin;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

public interface AdminCategoryService {

    CategoryDto addCategory(NewCategoryDto request);

    CategoryDto updateCategory(Long catId, CategoryDto request);

    void deleteCategory(Long catId);
}