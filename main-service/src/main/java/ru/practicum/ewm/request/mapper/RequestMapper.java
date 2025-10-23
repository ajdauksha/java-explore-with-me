package ru.practicum.ewm.request.mapper;

import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {

    public static ParticipationRequestDto.ParticipationRequestResponse toResponse(ParticipationRequest request) {
        return ParticipationRequestDto.ParticipationRequestResponse.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .build();
    }

    public static List<ParticipationRequestDto.ParticipationRequestResponse> toResponseList(List<ParticipationRequest> requests) {
        return requests.stream()
                .map(RequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ParticipationRequestDto.EventRequestStatusUpdateResult toUpdateResult(
            List<ParticipationRequest> confirmed, List<ParticipationRequest> rejected) {

        return ParticipationRequestDto.EventRequestStatusUpdateResult.builder()
                .confirmedRequests(toResponseList(confirmed))
                .rejectedRequests(toResponseList(rejected))
                .build();
    }
}