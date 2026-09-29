package ru.practicum.dto.compilation;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {

    private List<Long> events;

    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Длина должна быть от 1 до 50 символов")
    private String title;
}