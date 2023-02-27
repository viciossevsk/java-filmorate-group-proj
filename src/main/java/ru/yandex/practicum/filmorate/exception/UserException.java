package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }

    public UserException() {
    }
}
