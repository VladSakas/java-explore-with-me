package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.model.StatsEntity;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    private EndpointHit hit;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        hit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp(LocalDateTime.of(2022, 9, 6, 11, 0, 23))
                .build();

        start = LocalDateTime.of(2020, 5, 5, 0, 0);
        end = LocalDateTime.of(2035, 5, 5, 0, 0);
    }

    @Test
    void saveHit_shouldMapDtoToEntityAndSave() {
        statsService.saveHit(hit);

        verify(statsRepository, times(1)).save(any(StatsEntity.class));
    }

    @Test
    void getStats_whenUniqueTrue_shouldCallFindUniqueStats() {
        List<ViewStats> expected = List.of(new ViewStats("app", "/uri", 1L));
        when(statsRepository.findUniqueStats(eq(start), eq(end), any()))
                .thenReturn(expected);

        List<ViewStats> result = statsService.getStats(start, end, null, true);

        assertEquals(expected, result);
        verify(statsRepository, times(1)).findUniqueStats(start, end, null);
        verify(statsRepository, never()).findStats(any(), any(), any());
    }

    @Test
    void getStats_whenUniqueFalse_shouldCallFindStats() {
        List<ViewStats> expected = List.of(new ViewStats("app", "/uri", 5L));
        when(statsRepository.findStats(eq(start), eq(end), any()))
                .thenReturn(expected);

        List<ViewStats> result = statsService.getStats(start, end, null, false);

        assertEquals(expected, result);
        verify(statsRepository, times(1)).findStats(start, end, null);
        verify(statsRepository, never()).findUniqueStats(any(), any(), any());
    }

    @Test
    void getStats_whenStartAfterEnd_shouldThrowException() {
        LocalDateTime badStart = end.plusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> statsService.getStats(badStart, end, null, false));
    }
}