package ru.practicum.service.privateapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RatingMapper;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.Rating;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RatingRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateRatingServiceImpl implements PrivateRatingService {

    private final RatingRepository ratingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final RatingMapper ratingMapper;

    @Override
    @Transactional
    public RatingDto addRating(Long userId, Long eventId, Boolean isLike) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot rate own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot rate unpublished event");
        }

        boolean isParticipant = requestRepository
                .existsByEventIdAndRequesterIdAndStatus(eventId, userId, RequestStatus.CONFIRMED);

        if (!isParticipant) {
            throw new ConflictException("Only confirmed participants can rate event");
        }

        if (ratingRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new ConflictException("Rating already exists");
        }

        Rating rating = Rating.builder()
                .event(event)
                .user(user)
                .isLike(isLike)
                .created(LocalDateTime.now())
                .build();

        Rating saved = ratingRepository.save(rating);
        log.info("Создана оценка: id={}, event={}, user={}, isLike={}",
                saved.getId(), eventId, userId, isLike);
        return ratingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void removeRating(Long userId, Long eventId) {
        Rating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Rating for event=" + eventId + " by user=" + userId + " was not found"));

        ratingRepository.delete(rating);
        log.info("Удалена оценка: event={}, user={}", eventId, userId);
    }
}