package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEvent {
    private Long timestamp;
    private Integer userId;
    private String eventType;
    @JsonProperty("operation")
    private String operationType;
    @JsonProperty("eventId")
    private Integer id;
    private Integer entityId;
}