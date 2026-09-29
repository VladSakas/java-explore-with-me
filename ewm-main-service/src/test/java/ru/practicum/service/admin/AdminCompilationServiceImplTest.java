package ru.practicum.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCompilationServiceImplTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CompilationMapper compilationMapper;

    @InjectMocks
    private AdminCompilationServiceImpl adminCompilationService;

    private Compilation compilation;
    private CompilationDto compilationDto;
    private NewCompilationDto newCompilationDto;

    @BeforeEach
    void setUp() {
        compilation = Compilation.builder()
                .id(1L)
                .title("Летние концерты")
                .pinned(true)
                .events(Set.of())
                .build();

        compilationDto = CompilationDto.builder()
                .id(1L)
                .title("Летние концерты")
                .pinned(true)
                .build();

        newCompilationDto = NewCompilationDto.builder()
                .title("Летние концерты")
                .pinned(true)
                .build();
    }

    @Test
    void addCompilation_shouldCreate() {
        when(compilationRepository.existsByTitle("Летние концерты")).thenReturn(false);
        when(compilationMapper.toEntity(newCompilationDto, Set.of())).thenReturn(compilation);
        when(compilationRepository.save(compilation)).thenReturn(compilation);
        when(compilationMapper.toDto(compilation)).thenReturn(compilationDto);

        CompilationDto result = adminCompilationService.addCompilation(newCompilationDto);

        assertNotNull(result);
        assertEquals("Летние концерты", result.getTitle());
        verify(compilationRepository, times(1)).save(compilation);
    }

    @Test
    void addCompilation_withDuplicateTitle_shouldThrowConflict() {
        when(compilationRepository.existsByTitle("Летние концерты")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> adminCompilationService.addCompilation(newCompilationDto));
        verify(compilationRepository, never()).save(any());
    }

    @Test
    void deleteCompilation_notFound_shouldThrow() {
        when(compilationRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> adminCompilationService.deleteCompilation(999L));
        verify(compilationRepository, never()).deleteById(any());
    }
}