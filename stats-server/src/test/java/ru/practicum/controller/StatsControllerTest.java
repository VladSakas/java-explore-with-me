package ru.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsControllerTest {

    @Mock
    private StatsService statsService;

    @InjectMocks
    private StatsController statsController;

    private EndpointHit hit;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        hit = EndpointHit.builder()
                .app("app")
                .uri("/uri")
                .ip("1.1.1.1")
                .timestamp(LocalDateTime.now())
                .build();
        start = LocalDateTime.now().minusDays(1);
        end = LocalDateTime.now();
    }

    @Test
    void hit_shouldCallService() {
        statsController.hit(hit);
        verify(statsService, times(1)).saveHit(hit);
    }

    @Test
    void getStats_shouldCallService() {
        List<ViewStats> expected = List.of(new ViewStats("app", "/uri", 5L));
        when(statsService.getStats(start, end, null, false)).thenReturn(expected);

        List<ViewStats> result = statsController.getStats(start, end, null, false);

        assertEquals(expected, result);
        verify(statsService, times(1)).getStats(start, end, null, false);
    }
}