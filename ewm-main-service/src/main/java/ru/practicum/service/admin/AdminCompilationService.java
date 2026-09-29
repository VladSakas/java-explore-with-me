package ru.practicum.service.admin;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

public interface AdminCompilationService {

    CompilationDto addCompilation(NewCompilationDto request);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request);

    void deleteCompilation(Long compId);
}