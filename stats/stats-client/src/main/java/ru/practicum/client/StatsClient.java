package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.AddHitRequestDto;
import ru.practicum.dto.StatsResponseDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(String serverUrl) {
        this.restTemplate = new RestTemplate();
        this.serverUrl = serverUrl;
    }

    public void saveHit(AddHitRequestDto endpointHit) {
        log.debug("Saving hit: app={}, uri={}, ip={}",
                endpointHit.getApp(), endpointHit.getUri(), endpointHit.getIp());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<AddHitRequestDto> requestEntity = new HttpEntity<>(endpointHit, headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    serverUrl + "/hit",
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.debug("Hit successfully saved");
            } else {
                log.warn("Unexpected response status: {}", response.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            log.error("Error saving hit: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to save hit: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error saving hit: {}", e.getMessage());
            throw new RuntimeException("Unexpected error saving hit", e);
        }
    }

    public List<StatsResponseDto> getStats(LocalDateTime start, LocalDateTime end,
                                           List<String> uris, Boolean unique) {
        return getStats(start.format(formatter), end.format(formatter), uris, unique);
    }

    public List<StatsResponseDto> getStats(String start, String end,
                                    List<String> uris, Boolean unique) {
        log.debug("Getting stats: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", encodeValue(start))
                .queryParam("end", encodeValue(end));

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> builder.queryParam("uris", encodeValue(uri)));
        }

        if (unique != null) {
            builder.queryParam("unique", unique);
        }

        String url = builder.toUriString();
        log.debug("Request URL: {}", url);

        try {
            ResponseEntity<StatsResponseDto[]> response = restTemplate.getForEntity(url, StatsResponseDto[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<StatsResponseDto> stats = Arrays.asList(response.getBody());
                log.debug("Retrieved {} stats records", stats.size());
                return stats;
            } else {
                log.warn("Unexpected response: {}", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (HttpStatusCodeException e) {
            log.error("Error getting stats: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get stats: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error getting stats: {}", e.getMessage());
            throw new RuntimeException("Unexpected error getting stats", e);
        }
    }

    public List<StatsResponseDto> getStats(LocalDateTime start, LocalDateTime end) {
        return getStats(start, end, null, false);
    }

    public List<StatsResponseDto> getUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return getStats(start, end, uris, true);
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
