package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.DataConflictException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Transactional
    public Compilation createCompilation(Compilation compilation) {
        log.info("Creating compilation: {}", compilation.getTitle());

        if (compilationRepository.findByTitle(compilation.getTitle()).isPresent()) {
            throw new DataConflictException("Compilation with title " + compilation.getTitle() + " already exists");
        }

        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            Set<Long> eventIds = compilation.getEvents().stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());

            List<Event> events = eventRepository.findByIdIn(List.copyOf(eventIds));
            compilation.setEvents(Set.copyOf(events));
        }

        return compilationRepository.save(compilation);
    }

    @Transactional
    public void deleteCompilation(Long compilationId) {
        log.info("Deleting compilation with id: {}", compilationId);

        if (!compilationRepository.existsById(compilationId)) {
            throw new NoSuchElementException("Compilation not found with id: " + compilationId);
        }

        compilationRepository.deleteById(compilationId);
    }

    @Transactional
    public Compilation updateCompilation(Long compilationId, Compilation updatedCompilation) {
        log.info("Updating compilation with id: {}", compilationId);

        Compilation existing = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NoSuchElementException("Compilation not found with id: " + compilationId));

        if (updatedCompilation.getTitle() != null) {
            if (!existing.getTitle().equals(updatedCompilation.getTitle()) &&
                    compilationRepository.findByTitle(updatedCompilation.getTitle()).isPresent()) {
                throw new DataConflictException("Compilation with title " + updatedCompilation.getTitle() + " already exists");
            }
            existing.setTitle(updatedCompilation.getTitle());
        } else {
            existing.setTitle(existing.getTitle());
        }

        if (updatedCompilation.getPinned() != null) {
            existing.setPinned(updatedCompilation.getPinned());
        } else {
            existing.setPinned(existing.getPinned());
        }

        if (updatedCompilation.getEvents() != null && !updatedCompilation.getEvents().isEmpty()) {
            Set<Long> eventIds = updatedCompilation.getEvents().stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());

            List<Event> events = eventRepository.findByIdIn(new ArrayList<>(eventIds));
            existing.setEvents(new HashSet<>(events));
        } else {
            existing.setEvents(existing.getEvents());
        }

        return compilationRepository.save(existing);
    }

    public List<Compilation> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Getting compilations with pinned: {}, from: {}, size: {}", pinned, from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        return compilationRepository.findByPinned(pinned, pageable);
    }

    public Compilation getCompilationById(Long compilationId) {
        log.info("Getting compilation by id: {}", compilationId);

        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NoSuchElementException("Compilation not found with id: " + compilationId));
    }
}