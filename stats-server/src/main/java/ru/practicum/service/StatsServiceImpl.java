package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.BadRequestException;
import ru.practicum.model.StatsEntity;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHit endpointHit) {
        StatsEntity entity = StatsEntity.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
                .build();
        statsRepository.save(entity);
        log.info("Сохранён hit: app={}, uri={}, ip={}", entity.getApp(), entity.getUri(), entity.getIp());
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Start date must be before end date");
        }
        if (unique) {
            return statsRepository.findUniqueStats(start, end, uris);
        }
        return statsRepository.findStats(start, end, uris);
    }
}