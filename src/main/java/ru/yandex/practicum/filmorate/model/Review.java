package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
public class Review {
    @JsonProperty("reviewId")
    private Integer id;
    @NonNull
    @NotBlank
    @Size(max = 2000)
    private String content;
    private Boolean isPositive;
    private Integer userId;
    private Integer filmId;
    private Integer useful;
}
