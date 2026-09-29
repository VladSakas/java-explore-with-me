package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.dto.LocationDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Длина должна быть от 20 до 2000 символов")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Длина должна быть от 20 до 7000 символов")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Число должно быть положительным или нулём")
    private Integer participantLimit;

    private Boolean requestModeration;

    private String stateAction;

    @Size(min = 3, max = 120, message = "Длина должна быть от 3 до 120 символов")
    private String title;
}