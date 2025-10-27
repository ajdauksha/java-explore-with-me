package ru.practicum.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.AddHitRequestDto;
import ru.practicum.dto.StatsResponseDto;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.Hit;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final HitRepository hitRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    @Transactional
    public void saveHit(AddHitRequestDto hitRequestDto) {
        Hit hit = HitMapper.toHit(hitRequestDto);
        hitRepository.save(hit);
    }

    @Override
    public List<StatsResponseDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        LocalDateTime startTime = parseDateTime(start);
        LocalDateTime endTime = parseDateTime(end);

        if (startTime.isAfter(endTime)) {
            throw new ValidationException("Start time must be before end time");
        }

        if (Boolean.TRUE.equals(unique)) {
            return hitRepository.getUniqueStats(startTime, endTime, uris);
        } else {
            return hitRepository.getStats(startTime, endTime, uris);
        }
    }

    private LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, FORMATTER);
    }

}
