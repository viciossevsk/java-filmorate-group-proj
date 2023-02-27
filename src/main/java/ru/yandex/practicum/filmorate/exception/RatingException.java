package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RatingException extends RuntimeException {
    public RatingException() {
    }

    public RatingException(String message) {
        super(message);
    }
}