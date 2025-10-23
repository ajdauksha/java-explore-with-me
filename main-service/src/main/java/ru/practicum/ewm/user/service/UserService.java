package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.DataConflictException;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;

    @Transactional
    public User createUser(User user) {
        log.info("Creating user: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DataConflictException("User with email " + user.getEmail() + " already exists");
        }

        return userRepository.save(user);
    }

    public List<User> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Getting users with ids: {}, from: {}, size: {}", ids, from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(pageable).getContent();
        } else {
            return userRepository.findByIds(ids, pageable);
        }
    }

    public User getUserById(Long userId) {
        log.info("Getting user by id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found with id: " + userId);
        }

        userRepository.deleteById(userId);
    }

    @Transactional
    public void deleteUsers(List<Long> userIds) {
        log.info("Deleting users with ids: {}", userIds);

        for (Long userId : userIds) {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
            }
        }
    }
}