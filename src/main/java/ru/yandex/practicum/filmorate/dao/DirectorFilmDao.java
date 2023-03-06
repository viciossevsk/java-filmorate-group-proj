package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorFilmDao {

    void deleteAllDirectorsByFilm(Integer filmId);

    void addNewDirectorInFilm(Integer filmId, Integer directorId);

    List<Director> getDirectorsByFilm(Integer filmId);


}
