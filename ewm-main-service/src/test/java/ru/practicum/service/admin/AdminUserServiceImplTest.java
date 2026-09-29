package ru.practicum.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User user;
    private UserDto userDto;
    private NewUserRequest newUserRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Иван Петров")
                .email("ivan@mail.ru")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("Иван Петров")
                .email("ivan@mail.ru")
                .build();

        newUserRequest = NewUserRequest.builder()
                .name("Иван Петров")
                .email("ivan@mail.ru")
                .build();
    }

    @Test
    void addUser_shouldCreateUser() {
        when(userRepository.existsByEmail("ivan@mail.ru")).thenReturn(false);
        when(userMapper.toEntity(newUserRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = adminUserService.addUser(newUserRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ivan@mail.ru", result.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void addUser_withDuplicateEmail_shouldThrowConflict() {
        when(userRepository.existsByEmail("ivan@mail.ru")).thenReturn(true);

        assertThrows(ConflictException.class, () -> adminUserService.addUser(newUserRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUsers_withoutIds_shouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userMapper.toDto(user)).thenReturn(userDto);

        List<UserDto> result = adminUserService.getUsers(null, 0, 10);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void deleteUser_notFound_shouldThrow() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> adminUserService.deleteUser(999L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUser_shouldDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);

        adminUserService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}