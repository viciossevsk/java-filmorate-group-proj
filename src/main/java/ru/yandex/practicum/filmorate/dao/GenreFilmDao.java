package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreFilmDao {
    void addNewGenreInFilm(Integer filmId, Integer genreId);

    List<Genre> getGenresByFilm(Integer filmId);

    public void deleteGenreFilm(Integer filmId, Integer genreId);

    void deleteAllGenreByFilm(Integer filmId);
}
