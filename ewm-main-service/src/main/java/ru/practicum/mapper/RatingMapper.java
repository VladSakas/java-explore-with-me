package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.model.Rating;

@Component
public class RatingMapper {

    public RatingDto toDto(Rating rating) {
        return RatingDto.builder()
                .id(rating.getId())
                .event(rating.getEvent().getId())
                .user(rating.getUser().getId())
                .isLike(rating.getIsLike())
                .created(rating.getCreated())
                .build();
    }
}