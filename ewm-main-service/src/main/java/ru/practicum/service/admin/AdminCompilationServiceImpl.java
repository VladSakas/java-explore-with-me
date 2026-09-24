package ru.practicum.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCompilationServiceImpl implements AdminCompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto request) {
        if (compilationRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Подборка с названием " + request.getTitle() + " уже существует");
        }

        Set<Event> events = resolveEvents(request.getEvents());
        Compilation compilation = compilationMapper.toEntity(request, events);
        Compilation saved = compilationRepository.save(compilation);
        log.info("Создана подборка: id={}, title={}", saved.getId(), saved.getTitle());
        return compilationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));

        if (request.getTitle() != null && !compilation.getTitle().equals(request.getTitle())
                && compilationRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Подборка с названием " + request.getTitle() + " уже существует");
        }

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getEvents() != null) {
            Set<Event> events = resolveEvents(request.getEvents());
            compilation.setEvents(events);
        }

        Compilation updated = compilationRepository.save(compilation);
        log.info("Обновлена подборка: id={}, title={}", updated.getId(), updated.getTitle());
        return compilationMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка с id " + compId + " не найдена");
        }
        compilationRepository.deleteById(compId);
        log.info("Удалена подборка: id={}", compId);
    }

    private Set<Event> resolveEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Event> events = eventRepository.findAllById(eventIds);
        if (events.size() != eventIds.size()) {
            throw new NotFoundException("События не найдены");
        }
        return new HashSet<>(events);
    }
}