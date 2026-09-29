package ru.practicum.service.publicapi;

import ru.practicum.dto.compilation.CompilationDto;

import java.util.List;

public interface PublicCompilationService {

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(Long compId);
}