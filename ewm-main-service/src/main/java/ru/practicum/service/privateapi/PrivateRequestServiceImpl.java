package ru.practicum.service.privateapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateRequestServiceImpl implements PrivateRequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        checkUserExists(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot add request to own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }

        RequestStatus status;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = RequestStatus.PENDING;
        }

        Request request = Request.builder()
                .event(event)
                .requester(requester)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        Request saved = requestRepository.save(request);
        log.info("Создана заявка: id={}, event={}, requester={}, status={}",
                saved.getId(), eventId, userId, saved.getStatus());
        return requestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUserExists(userId);
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        request.setStatus(RequestStatus.CANCELED);
        Request updated = requestRepository.save(request);
        log.info("Отменена заявка: id={}", updated.getId());
        return requestMapper.toDto(updated);
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }
}