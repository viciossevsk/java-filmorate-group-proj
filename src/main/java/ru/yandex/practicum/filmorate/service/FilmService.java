package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToGreenColor;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    public List<Film> getAllFilms() {
        log.info(stringToGreenColor("call method getAllFilms in FilmStorage... via GET /films"));
        return filmStorage.getAllFilms();
    }
    public Film createFilm(Film film) {
        log.info(stringToGreenColor("call method add film in FilmStorage... via POST /film"));
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        validateFilm(film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        log.info(stringToGreenColor("call method update film in FilmStorage... via PUT /film"));
        Film filmExist = filmStorage.getFilmById(film.getId());
        validateFilm(film);
        return filmStorage.updateFilm(film);
    }
    public void addLikeToFilm(Integer filmId, Integer userId) {
        log.info(stringToGreenColor("add like film..."));
        Film filmExist = filmStorage.getFilmById(filmId);
        User userExist = userStorage.getUserById(userId);
        filmStorage.addLikeToFilm(filmId, userId);
    }

    public Film getFilmById(Integer id) {
        return filmStorage.getFilmById(id);
    }

    public void removeLikeFromFilm(Integer filmId, Integer userId) {
        log.info(stringToGreenColor("remove like from film..."));
        Film filmExist = filmStorage.getFilmById(filmId);
        User userExist = userStorage.getUserById(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public Genre getGenreById(int genreId) {
        return filmStorage.getGenreById(genreId);
    }

    /**
     * сортируем DESC
     */
    public List<Film> getMostPopularFilms(Integer count) {
        log.info(stringToGreenColor("getAllFilms... "));
        return filmStorage.getMostPopularFilms(count);
    }

    public List<Rating> getAllRatings() {
        return filmStorage.getAllRatings();
    }

    public Rating getRatingById(int ratingId) {
        return filmStorage.getRatingById(ratingId);
    }

    private boolean validateFilm(Film film) throws ValidationException {
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Film description may not exceed 200 symbols");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Invalid film release date");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Duration must be positive number");
        }
        return true;
    }
}
