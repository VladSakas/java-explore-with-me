package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.Request;

@Component
public class RequestMapper {

    public ParticipationRequestDto toDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .created(request.getCreated())
                .build();
    }
}