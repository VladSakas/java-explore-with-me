package ru.practicum.service.privateapi;

import ru.practicum.dto.rating.RatingDto;

public interface PrivateRatingService {

    RatingDto addRating(Long userId, Long eventId, Boolean isLike);

    void removeRating(Long userId, Long eventId);
}