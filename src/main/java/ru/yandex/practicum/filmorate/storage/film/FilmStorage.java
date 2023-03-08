package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;

public interface FilmStorage {

    List<Film> getAllFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(Integer filmId);

    List<Genre> getAllGenres();

    Genre getGenreById(Integer genreId);

    List<Rating> getAllRatings();

    Rating getRatingById(Integer ratingId);

    //  List<Film> getMostPopularFilms(Integer count);
    List<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year);

    List<Film> getCommonFilms(Integer userId, Integer friendId);

    void addLikeToFilm(int filmId, int userId);

    void deleteFilmById(Integer filmId);

    void removeLike(int filmId, int userId);

    List<Film> getRecommendations(Integer id);

    List<Film> getFilmsDirectorsSortBy(Integer directorId, String sortBy);

    List<Film> searchFilmByDirector(String director);

    List<Film> searchFilmByTitle(String title);

    List<Film> searchByTitleDirector(String dirtit);

}

