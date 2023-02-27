package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilmException extends RuntimeException {

    public FilmException() {
    }

    public FilmException(String message) {
        super(message);
    }
}
