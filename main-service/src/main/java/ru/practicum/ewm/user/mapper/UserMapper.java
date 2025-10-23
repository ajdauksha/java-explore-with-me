package ru.practicum.ewm.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

@UtilityClass
public class UserMapper {

    public static User toEntity(UserDto.NewUserRequest dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static UserDto.UserResponse toResponse(User user) {
        return UserDto.UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static UserDto.UserShortDto toShortDto(User user) {
        return UserDto.UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}