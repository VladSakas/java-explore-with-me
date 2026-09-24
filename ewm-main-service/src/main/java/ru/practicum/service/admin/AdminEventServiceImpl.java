package ru.practicum.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.*;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final RequestRepository requestRepository;

    @Override
    public List<EventFullDto> getEvents(List<Long> users,
                                        List<String> states,
                                        List<Long> categories,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        int from,
                                        int size) {
        List<EventState> stateEnums = null;
        if (states != null && !states.isEmpty()) {
            stateEnums = states.stream()
                    .map(this::parseState)
                    .collect(Collectors.toList());
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Начало должно быть раньше конца");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findEventsForAdmin(
                users, stateEnums, categories, rangeStart, rangeEnd, pageable);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toFullDto(event);
                    dto.setConfirmedRequests(
                            requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED));
                    dto.setViews(0L);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        applyUpdates(event, request);
        applyStateAction(event, request.getStateAction());

        Event updated = eventRepository.save(event);
        log.info("Обновлено событие: id={}, state={}", updated.getId(), updated.getState());
        return eventMapper.toFullDto(updated);
    }

    private void applyUpdates(Event event, UpdateEventAdminRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            "Категория с id " + request.getCategory() + " не найдена"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new BadRequestException("дата начала изменяемого события должна быть не ранее чем за час " +
                        "от даты публикации");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(new Location(
                    request.getLocation().getLat(),
                    request.getLocation().getLon()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    private void applyStateAction(Event event, String stateAction) {
        if (stateAction == null) {
            return;
        }

        switch (stateAction) {
            case "PUBLISH_EVENT" -> {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException(
                            "событие можно публиковать, только если оно в состоянии ожидания публикации: "
                            + event.getState());
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException(
                            "дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            case "REJECT_EVENT" -> {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("событие можно отклонить, только если оно еще не опубликовано");
                }
                event.setState(EventState.CANCELED);
            }
            default -> throw new BadRequestException("Неизвестный статус: " + stateAction);
        }
    }

    private EventState parseState(String state) {
        try {
            return EventState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Неизвестный статус: " + state);
        }
    }
}