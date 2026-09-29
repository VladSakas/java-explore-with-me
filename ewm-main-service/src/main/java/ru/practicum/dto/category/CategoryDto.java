package ru.practicum.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min = 1, max = 50, message = "Имя должно быть длиной от 1 до 50 символов")
    private String name;
}
