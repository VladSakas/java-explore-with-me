package ru.practicum.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private AdminCategoryServiceImpl adminCategoryService;

    private Category category;
    private CategoryDto categoryDto;
    private NewCategoryDto newCategoryDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();

        newCategoryDto = NewCategoryDto.builder()
                .name("Концерты")
                .build();
    }

    @Test
    void addCategory_shouldCreate() {
        when(categoryRepository.existsByName("Концерты")).thenReturn(false);
        when(categoryMapper.toEntity(newCategoryDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        CategoryDto result = adminCategoryService.addCategory(newCategoryDto);

        assertNotNull(result);
        assertEquals("Концерты", result.getName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void deleteCategory_withEvents_shouldThrowConflict() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> adminCategoryService.deleteCategory(1L));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void updateCategory_notFound_shouldThrow() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> adminCategoryService.updateCategory(999L, categoryDto));
        verify(categoryRepository, never()).save(any());
    }
}