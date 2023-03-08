package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToBlueColor;
import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToGreenColor;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    @Override
    public List<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year) {
        log.info(stringToGreenColor("getMostPopularFilms... "));
        return getAllFilms().stream().sorted(Comparator.comparing(film -> film.getLikes().size() * -1)).limit(count).collect(Collectors.toList());
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        return null;
    }

    private final Map<Integer, Genre> genres = new HashMap<>();
    private final Map<Integer, Rating> ratings = new HashMap<>();
    private final Map<Integer, Film> films = new HashMap<>();
    private int generatorId;


    public InMemoryFilmStorage() {
        initiateGenres();
        initiateRatings();
    }

    @Override
    public void deleteFilmById(Integer filmId) {
        log.info(stringToGreenColor("delete film... via DELETE /film"));
        Film film = getFilmById(filmId);
        films.remove(film.getId());
    }

    @Override
    public void removeLike(int filmId, int userId) {
        this.getFilmById(filmId).removeLike(userId);
    }

    @Override
    public List<Film> getRecommendations(Integer id) {
        return new ArrayList<>();
    }

    @Override
    public void addLikeToFilm(int filmId, int userId) {
        this.getFilmById(filmId).addLike(userId);
    }

    private void initiateGenres() {
        genres.put(1, Genre.builder().id(1).name("Комедия").build());
        genres.put(2, Genre.builder().id(2).name("Драма").build());
        genres.put(3, Genre.builder().id(3).name("Мультфильм").build());
        genres.put(4, Genre.builder().id(4).name("Триллер").build());
        genres.put(5, Genre.builder().id(5).name("Документальный").build());
        genres.put(6, Genre.builder().id(6).name("Боевик").build());
    }

    private void initiateRatings() {
        ratings.put(1, Rating.builder().id(1).name("G").build());
        ratings.put(2, Rating.builder().id(2).name("PG").build());
        ratings.put(3, Rating.builder().id(3).name("PG-13").build());
        ratings.put(4, Rating.builder().id(4).name("R").build());
        ratings.put(5, Rating.builder().id(5).name("NC-17").build());
    }


    @Override
    public Film getFilmById(Integer id) {
        if (id != null) {
            if (films.containsKey(id)) {
                return films.get(id);
            } else {
                throw new FilmException("film id is empty");
            }
        } else {
            throw new FilmNotFoundException("film id=" + id + " not found");
        }
    }

    @Override
    public Genre getGenreById(Integer id) {
        if (id != null) {
            if (genres.containsKey(id)) {
                return genres.get(id);
            } else {
                throw new GenreException("Genre id is empty");
            }
        } else {
            throw new GenreNotFoundException("Genre id=" + id + " not found");
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public List<Film> getAllFilms() {
        log.info(stringToGreenColor("getAllFilms... via GET /films"));
        return new ArrayList<>(films.values());
    }

    @Override
    public Film createFilm(Film film) {
        validate(film);
        film.setId(++generatorId);
        films.put(film.getId(), film);
        log.info(stringToGreenColor("add film... via POST /film"));
        log.info(stringToBlueColor(film.toString()));
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info(stringToGreenColor("update film... via PUT /film"));
        validate(film);
        if (film.getId() != null) {
            if (films.containsKey(film.getId())) {
                films.replace(film.getId(), film);
            } else {
                throw new FilmException("film id invalid");
            }
        } else {
            throw new FilmNotFoundException("film id not found");
        }
        return film;
    }

    public void validate(Film film) {
        log.trace(stringToGreenColor("validate for film"));
        if (film.getName().isEmpty()) {
            throw new ValidationException("film name invalid");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("film description length > 200");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("film releaseDate < 28.12.1985");
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("film duration < 0");
        }
    }

    @Override
    public List<Film> getFilmsDirectorsSortBy(Integer directorId, String sortBy) {
        log.info(stringToGreenColor("getFilmsDirectorsSortBy... via GET /films"));
        return new ArrayList<>(films.values());
    }

    @Override
    public List<Film> searchFilmByDirector(String director) {
        return null;
    }

    @Override
    public List<Film> searchFilmByTitle(String title) {
        return null;
    }

    @Override
    public List<Film> searchByTitleDirector(String dirtit) {
        return null;
    }

    @Override
    public List<Rating> getAllRatings() {
        return new ArrayList<>(ratings.values());
    }

    @Override
    public Rating getRatingById(Integer id) {
        if (id != null) {
            if (ratings.containsKey(id)) {
                return ratings.get(id);
            } else {
                throw new RatingException("Rating id is empty");
            }
        } else {
            throw new RatingNotFoundException("Rating id=" + id + " not found");
        }
    }
}