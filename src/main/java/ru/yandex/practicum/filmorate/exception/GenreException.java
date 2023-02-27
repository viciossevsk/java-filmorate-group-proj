package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenreException extends RuntimeException {
    public GenreException() {
    }

    public GenreException(String message) {
        super(message);
    }
}