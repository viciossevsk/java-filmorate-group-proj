package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class User {
    private Integer id;
    @NonNull
    @NotBlank
    @Email
    private String email;
    @NonNull
    @NotBlank
    private String login;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private Set<Integer> friends;

}
