package ru.practicum.service.publicapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.RequestStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private static final String APP_NAME = "ewm-main-service";

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         String sort,
                                         int from,
                                         int size,
                                         String ip) {
        statsClient.saveHit(APP_NAME, "/events", ip, LocalDateTime.now());

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }

        LocalDateTime effectiveStart = rangeStart != null ? rangeStart : LocalDateTime.now();

        List<Event> events = eventRepository.findPublishedEvents(
                text, categories, paid, effectiveStart, rangeEnd, null);

        Map<Long, Long> viewsMap = loadViews(events);

        Map<Long, Long> confirmedMap = loadConfirmedRequests(events);

        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream()
                    .filter(e -> isAvailable(e, confirmedMap.getOrDefault(e.getId(), 0L)))
                    .collect(Collectors.toList());
        }

        if ("VIEWS".equals(sort)) {
            events.sort(Comparator.comparingLong(
                    (Event e) -> viewsMap.getOrDefault(e.getId(), 0L)).reversed());
        } else {
            events.sort(Comparator.comparing(Event::getEventDate));
        }

        int startIdx = Math.min(from, events.size());
        int endIdx = Math.min(from + size, events.size());
        List<Event> paged = events.subList(startIdx, endIdx);

        return paged.stream()
                .map(e -> {
                    EventShortDto dto = eventMapper.toShortDto(e);
                    dto.setViews(viewsMap.getOrDefault(e.getId(), 0L));
                    dto.setConfirmedRequests(confirmedMap.getOrDefault(e.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEvent(Long eventId, String ip) {
        // Сохраняем hit
        statsClient.saveHit(APP_NAME, "/events/" + eventId, ip, LocalDateTime.now());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setViews(loadViews(List.of(event)).getOrDefault(eventId, 0L));
        dto.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
        return dto;
    }

    private Map<Long, Long> loadViews(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());

        List<ViewStats> stats = statsClient.getStats(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                uris,
                true);

        Map<String, Long> uriToHits = stats.stream()
                .collect(Collectors.toMap(ViewStats::getUri, ViewStats::getHits));

        return events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> uriToHits.getOrDefault("/events/" + e.getId(), 0L)));
    }

    private Map<Long, Long> loadConfirmedRequests(List<Event> events) {
        return events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED)));
    }

    private boolean isAvailable(Event event, long confirmed) {
        return event.getParticipantLimit() == 0 || confirmed < event.getParticipantLimit();
    }
}