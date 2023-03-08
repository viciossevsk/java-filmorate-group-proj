package ru.yandex.practicum.filmorate.exception;

public class DirectorNotFoundException extends RuntimeException {
    public DirectorNotFoundException() {
    }

    public DirectorNotFoundException(String message) {
        super(message);
    }
}