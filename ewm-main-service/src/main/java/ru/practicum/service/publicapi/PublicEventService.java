package ru.practicum.service.publicapi;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventService {

    List<EventShortDto> getEvents(String text,
                                  List<Long> categories,
                                  Boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Boolean onlyAvailable,
                                  String sort,
                                  int from,
                                  int size,
                                  String ip);

    EventFullDto getEvent(Long eventId, String ip);
}