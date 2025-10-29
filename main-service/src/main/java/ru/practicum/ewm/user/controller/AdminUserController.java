package ru.practicum.ewm.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto.UserResponse createUser(@Valid @RequestBody UserDto.NewUserRequest userRequest) {
        log.info("Admin: creating user {}", userRequest.getEmail());
        User user = UserMapper.toEntity(userRequest);
        User created = userService.createUser(user);
        return UserMapper.toResponse(created);
    }

    @GetMapping
    public List<UserDto.UserResponse> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Admin: getting users with ids: {}, from: {}, size: {}", ids, from, size);
        List<User> users = userService.getUsers(ids, from, size);
        return users.stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Admin: deleting user with id: {}", userId);
        userService.deleteUser(userId);
    }
}