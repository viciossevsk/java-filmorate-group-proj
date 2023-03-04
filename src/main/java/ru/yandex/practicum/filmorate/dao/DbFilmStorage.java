package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Component
@Primary
@Slf4j
public class DbFilmStorage implements FilmStorage {
    private final static String GET_ALL_FILMS_SQL = "select f.film_id," +
            "f.name, " +
            "f.description, " +
            "f.release_date, " +
            "f.duration, " +
            "r.rating_id AS rating_id, " +
            "r.name AS rating_name FROM FILM f " +
            "left JOIN RATING r ON f.RATING_ID = r.RATING_ID";

    private final static String CHECK_EXIST_FILM_SQL = "select count(*) as cnt from film where film_id = ?";
    private final static String DELETE_FILM_SQL = "delete from film where film_id = ?";
    private final static String GET_FILM_BY_ID_SQL = GET_ALL_FILMS_SQL + " where f.film_id = ?";
    private final static String GET_POPULAR_FILMS_SQL = "select f.film_id, " +
            "f.name, " +
            "f.description, " +
            "f.release_date, " +
            "f.duration, " +
            "r.rating_id as rating_id, " +
            "r.name as rating_name " +
            "from film f " +
            "inner join rating r using(rating_id) " +
            "left join film_likes fl using(film_id) " +
            "group by f.film_id " +
            "order by count(distinct fl.users_id) DESC " +
            "limit ?";
    private final static String SET_NEW_FILM_SQL = "insert into film " +
            "(name, description, release_date, duration, rating_id) " +
            "values(?, ?, ?, ?, ?)";
    private final static String UPDATE_FILM_SQL = "update film set " +
            "name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
            "where film_id = ?";
    private final JdbcTemplate jdbcTemplate;
    private final RatingDao ratingDao;
    private final GenreDao genreDao;
    private final GenreFilmDao genreFilmDao;
    private final FilmLikesDao filmLikesDao;

    public DbFilmStorage(JdbcTemplate jdbcTemplate, RatingDao ratingDao, GenreDao genreDao, GenreFilmDao genreFilmDao
            , FilmLikesDao filmLikesDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.ratingDao = ratingDao;
        this.genreDao = genreDao;
        this.genreFilmDao = genreFilmDao;
        this.filmLikesDao = filmLikesDao;
    }

    @Override
    public List<Film> getAllFilms() {
        return jdbcTemplate.query(GET_ALL_FILMS_SQL, (rs, rowNum) -> buildFilm(rs));
    }

    @Override
    public Film createFilm(Film film) throws FilmException {
        KeyHolder filmKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(SET_NEW_FILM_SQL, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, filmKeyHolder);
        Integer filmId = Objects.requireNonNull(filmKeyHolder.getKey()).intValue();
        if (film.getGenres() != null) {
            film.getGenres().stream().
                    forEach(genre -> genreFilmDao.addNewGenreInFilm(filmId, genre.getId()));
        }
        film.setId(filmId);
        log.info("The following film was successfully added: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) throws FilmException {
        if (checkFilmExist(film.getId())) {
        jdbcTemplate.update(UPDATE_FILM_SQL
                , film.getName()
                , film.getDescription()
                , Date.valueOf(film.getReleaseDate())
                , film.getDuration()
                , film.getMpa().getId()
                , film.getId());

        genreFilmDao.deleteAllGenreByFilm(film.getId());

        if (film.getGenres() != null) {
            film.getGenres().stream()
                    .forEach((genre) -> {
                        genreFilmDao.addNewGenreInFilm(film.getId(), genre.getId());
                    });
        }

        filmLikesDao.deleteAllLikesByFilm(film.getId());

        if (film.getLikes() != null) {
            film.getLikes().stream()
                    .forEach((like) -> {
                        genreFilmDao.addNewGenreInFilm(film.getId(), like);
                    });
        }
        log.info("The following film was successfully updated: {}", film);
        return film;
        } else {
            throw new FilmNotFoundException("Film with id=" + film.getId() + " not found");
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        filmLikesDao.removeLikeFromFilm(filmId, userId);
    }

    @Override
    public void deleteFilmById(Integer filmId) {
        if (checkFilmExist(filmId)) {
            Film film = jdbcTemplate.queryForObject(GET_FILM_BY_ID_SQL, (rs, rowNum) -> buildFilm(rs), filmId);
            jdbcTemplate.update(DELETE_FILM_SQL, filmId);
        } else {
            throw new FilmNotFoundException("Film with id=" + filmId + " not found");
        }
    }

    @Override
    public Film getFilmById(Integer filmId) {
        if (checkFilmExist(filmId)) {
            Film film = jdbcTemplate.queryForObject(GET_FILM_BY_ID_SQL, (rs, rowNum) -> buildFilm(rs), filmId);
            return film;
        } else {
            throw new FilmNotFoundException("Film with id=" + filmId + " not found");
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        return genreDao.getAllGenres();
    }

    @Override
    public Genre getGenreById(Integer genreId) {
        return genreDao.getGenreById(genreId);
    }

    @Override
    public List<Rating> getAllRatings() {
        return ratingDao.getAllRatings();
    }

    @Override
    public Rating getRatingById(Integer ratingId) {
        return ratingDao.getRatingById(ratingId);
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        return jdbcTemplate.query(GET_POPULAR_FILMS_SQL, (rs, rowNum) -> buildFilm(rs), count);
    }

    @Override
    public void addLikeToFilm(int filmId, int userId) {
        filmLikesDao.addLikeToFilm(filmId, userId);
    }

    private Film buildFilm(ResultSet rs) throws SQLException {
        int id = rs.getInt("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        int ratingId = rs.getInt("rating_id");
        String ratingName = rs.getString("rating_name");
        Rating rating = Rating.builder()
                .id(ratingId)
                .name(ratingName)
                .build();
        Film result = Film.builder()
                .id(id)
                .name(name)
                .description(description)
                .releaseDate(releaseDate)
                .duration(duration)
                .mpa(rating)
                .build();
        Set<Integer> usersLikes = new HashSet<>(filmLikesDao.getUserLikesByFilm(result.getId()));
        LinkedHashSet<Genre> genres = new LinkedHashSet<>(genreFilmDao.getGenresByFilm(result.getId()));
        result.setLikes(usersLikes);
        result.setGenres(genres);
        return result;
    }

    private Boolean checkFilmExist(Integer filmId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_EXIST_FILM_SQL, Integer.class, filmId);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

}
