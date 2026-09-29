package ru.practicum.service.privateapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        checkUserExists(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto request) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Category category = categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));

        if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + request.getEventDate());
        }

        Event event = eventMapper.toEntity(request);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        Event saved = eventRepository.save(event);
        log.info("Создано событие: id={}, state={}", saved.getId(), saved.getState());
        return eventMapper.toFullDto(saved);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        checkUserExists(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        return eventMapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        checkUserExists(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        applyUserUpdates(event, request);
        applyUserStateAction(event, request.getStateAction());

        Event updated = eventRepository.save(event);
        log.info("Обновлено событие пользователем: id={}, state={}", updated.getId(), updated.getState());
        return eventMapper.toFullDto(updated);
    }

    private void applyUserUpdates(Event event, UpdateEventUserRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + request.getEventDate());
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

    private void applyUserStateAction(Event event, String stateAction) {
        if (stateAction == null) {
            return;
        }
        switch (stateAction) {
            case "SEND_TO_REVIEW" -> event.setState(EventState.PENDING);
            case "CANCEL_REVIEW" -> event.setState(EventState.CANCELED);
            default -> throw new BadRequestException("Unknown stateAction: " + stateAction);
        }
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        checkUserExists(userId);
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        checkUserExists(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Request moderation is disabled for this event");
        }

        List<Request> requests = requestRepository.findAllById(request.getRequestIds());
        if (requests.size() != request.getRequestIds().size()) {
            throw new NotFoundException("Some requests not found");
        }

        for (Request r : requests) {
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
            if (!r.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Request does not belong to this event");
            }
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        if ("CONFIRMED".equals(request.getStatus())) {
            for (Request r : requests) {
                if (confirmedCount >= event.getParticipantLimit()) {
                    r.setStatus(RequestStatus.REJECTED);
                    rejected.add(requestMapper.toDto(r));
                } else {
                    r.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(requestMapper.toDto(r));
                    confirmedCount++;
                }
            }
        } else if ("REJECTED".equals(request.getStatus())) {
            for (Request r : requests) {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(requestMapper.toDto(r));
            }
        } else {
            throw new BadRequestException("Unknown status: " + request.getStatus());
        }

        requestRepository.saveAll(requests);

        if ("CONFIRMED".equals(request.getStatus()) && confirmedCount >= event.getParticipantLimit()) {
            List<Request> pending = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
            for (Request r : pending) {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(requestMapper.toDto(r));
            }
            requestRepository.saveAll(pending);
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }
}