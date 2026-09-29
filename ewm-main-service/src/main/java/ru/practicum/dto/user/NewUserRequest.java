package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 250, message = "Имя должно быть длиной от 2 до 250 символов")
    private String name;

    @NotBlank(message = "Емейл не может быть пустым")
    @Email(message = "Адрес почты некорректен")
    @Size(min = 6, max = 254, message = "Емейл должен быть длиной от 6 до 254 символов")
    private String email;

}