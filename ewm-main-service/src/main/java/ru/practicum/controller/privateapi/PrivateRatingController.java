package ru.practicum.controller.privateapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.service.privateapi.PrivateRatingService;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/rating")
@RequiredArgsConstructor
@Slf4j
public class PrivateRatingController {

    private final PrivateRatingService privateRatingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto addRating(@PathVariable Long userId,
                               @PathVariable Long eventId,
                               @RequestParam Boolean isLike) {
        log.info("POST /users/{}/events/{}/rating?isLike={}", userId, eventId, isLike);
        return privateRatingService.addRating(userId, eventId, isLike);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRating(@PathVariable Long userId,
                             @PathVariable Long eventId) {
        log.info("DELETE /users/{}/events/{}/rating", userId, eventId);
        privateRatingService.removeRating(userId, eventId);
    }
}