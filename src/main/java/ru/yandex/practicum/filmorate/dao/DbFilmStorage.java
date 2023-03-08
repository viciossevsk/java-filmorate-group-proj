package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
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

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToGreenColor;

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

    private final static String GET_POPULAR_FILMS_SQL2 = "select f.film_id, " +
            "f.name, " +
            "f.description, " +
            "f.release_date, " +
            "f.duration, " +
            "r.rating_id as rating_id, " +
            "r.name as rating_name " +
            "from film f " +
            "inner join rating r using(rating_id) " +
            "left join film_likes fl using(film_id) " +
            "left join GENRE_FILM gf using(film_id) " +
            "WHERE (? is NULL OR gf.GENRE_ID = ?) " +
            "AND (? is NULL OR EXTRACT(year FROM f.release_date) = ?) " +
            "group by f.film_id " +
            "order by count(distinct fl.users_id) DESC " +
            "limit ?";

    private final static String SET_NEW_FILM_SQL = "insert into film " +
            "(name, description, release_date, duration, rating_id) " +
            "values(?, ?, ?, ?, ?)";
    private final static String UPDATE_FILM_SQL = "update film set " +
            "name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
            "where film_id = ?";
    private final static String GET_SORT_LIKES_FILMS_BY_DIRECTOR_SQL =
            "select f.film_id, " +
                    "f.name, " +
                    "f.description, " +
                    "f.release_date, " +
                    "f.duration, " +
                    "r.rating_id as rating_id, " +
                    "r.name as rating_name " +
                    "from film f " +
                    "inner join rating r using(rating_id) " +
                    "inner JOIN DIRECTOR_FILM df using(film_id) " +
                    "left join film_likes fl using(film_id) " +
                    "WHERE df.DIRECTOR_ID = ? " +
                    "group by f.film_id " +
                    "order by count(distinct fl.users_id)";
    private final static String GET_SORT_YEAR_FILMS_BY_DIRECTOR_SQL =
            "select f.film_id, " +
                    "f.name, " +
                    "f.description, " +
                    "f.release_date, " +
                    "f.duration, " +
                    "r.rating_id as rating_id, " +
                    "r.name as rating_name " +
                    "from film f " +
                    "inner join rating r using(rating_id) " +
                    "inner JOIN DIRECTOR_FILM df using(film_id) " +
                    "left join film_likes fl using(film_id) " +
                    "WHERE df.DIRECTOR_ID = ? " +
                    "group by f.film_id " +
                    "order by RELEASE_DATE";

    private final static String GET_COMMON_FILMS_SQL =
            "select fl.film_id, " +
                    "f.name, " +
                    "f.description, " +
                    "f.release_date, " +
                    "f.duration, " +
                    "r.rating_id as rating_id, " +
                    "r.name as rating_name, " +
                    "count (fl.film_id) as cnt " +
                    "from film_likes fl " +
                    "left join film f on fl.film_id = f.film_id " +
                    "left join rating r on f.rating_id  = r.rating_id " +
                    "where fl.film_id in " +
                    "(select fl.film_id " +
                    "from users u " +
                    "left join film_likes fl on fl.users_id = u.users_id " +
                    "where u.users_id = ? and fl.film_id in " +
                    "(select film_id " +
                    "from users u " +
                    "left join film_likes fl ON fl.users_id = u.users_id " +
                    "where u.users_id = ?)) " +
                    "group by fl.film_id " +
                    "order by cnt desc";

    private final static String SEARCH_FILMS_BY_DIRECTOR =
            "SELECT F.*, R.NAME as rating_name, r.rating_id as rating_id, D.NAME as director_name " +
                    "FROM FILM AS F " +
                    "JOIN RATING AS R on F.RATING_ID = R.RATING_ID " +
                    "LEFT JOIN FILM_LIKES AS L on F.FILM_ID = L.FILM_ID " +
                    "LEFT JOIN DIRECTOR_FILM AS DF ON F.FILM_ID = DF.FILM_ID " +
                    "LEFT JOIN DIRECTOR AS D ON D.DIRECTOR_ID = DF.DIRECTOR_ID " +
                    "WHERE LOWER(D.NAME) LIKE ? " +
                    "GROUP BY F.FILM_ID ORDER BY COUNT(L.USERS_ID) DESC";

    private final static String SEARCH_FILMS_BY_TITLE =
            "SELECT F.*, R.NAME as rating_name, r.rating_id as rating_id " +
                    "FROM FILM AS F " +
                    "JOIN RATING AS R on F.RATING_ID = R.RATING_ID " +
                    "LEFT JOIN FILM_LIKES AS L on F.FILM_ID = L.FILM_ID " +
                    "WHERE LOWER(F.NAME) LIKE ? " +
                    "GROUP BY F.FILM_ID ORDER BY COUNT(L.USERS_ID) DESC ";

    private final static String SEARCH_FILMS_BY_TITLE_DIRECTOR =
            "SELECT F.*, R.NAME as rating_name, r.rating_id as rating_id, D.NAME as director_name " +
                    "FROM FILM AS F " +
                    "LEFT JOIN RATING AS R on F.RATING_ID = R.RATING_ID " +
                    "LEFT JOIN FILM_LIKES AS L on F.FILM_ID = L.FILM_ID " +
                    "LEFT JOIN DIRECTOR_FILM AS DF ON F.FILM_ID = DF.FILM_ID " +
                    "LEFT JOIN DIRECTOR AS D ON D.DIRECTOR_ID = DF.DIRECTOR_ID " +
                    "WHERE LOWER(D.NAME) LIKE ? OR LOWER(F.NAME) LIKE ? " +
                    "GROUP BY F.FILM_ID ORDER BY COUNT(L.USERS_ID) DESC";

    private final JdbcTemplate jdbcTemplate;
    private final RatingDao ratingDao;
    private final GenreDao genreDao;
    private final GenreFilmDao genreFilmDao;
    private final FilmLikesDao filmLikesDao;
    private final DirectorDao directorDao;
    private final DirectorFilmDao directorFilmDao;

    public DbFilmStorage(JdbcTemplate jdbcTemplate, RatingDao ratingDao, GenreDao genreDao, GenreFilmDao genreFilmDao
            , FilmLikesDao filmLikesDao, DirectorDao directorDao, DirectorFilmDao directorFilmDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.ratingDao = ratingDao;
        this.genreDao = genreDao;
        this.genreFilmDao = genreFilmDao;
        this.filmLikesDao = filmLikesDao;
        this.directorDao = directorDao;
        this.directorFilmDao = directorFilmDao;
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
        } else {
            film.setGenres(new LinkedHashSet<>());
        }
        if (film.getDirectors() != null) {
            film.getDirectors().stream().
                    forEach(director -> directorFilmDao.addNewDirectorInFilm(filmId, director.getId()));
        } else {
            film.setDirectors(new LinkedHashSet<>());
        }
        film.setId(filmId);
        log.info("The following film was successfully added: {}", film);
        return film;
    }

    @Override
    public List<Film> getFilmsDirectorsSortBy(Integer directorId, String sortBy) {
        if (directorDao.checkDirectorExist(directorId)) {
            if (sortBy.equals("likes")) {
                return jdbcTemplate.query(GET_SORT_LIKES_FILMS_BY_DIRECTOR_SQL, (rs, rowNum) -> buildFilm(rs),
                                          directorId);
            } else {
                return jdbcTemplate.query(GET_SORT_YEAR_FILMS_BY_DIRECTOR_SQL, (rs, rowNum) -> buildFilm(rs),
                                          directorId);
            }
        } else {
            throw new DirectorNotFoundException("Director with id=" + directorId + "not found");
        }
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
            } else {
                film.setGenres(new LinkedHashSet<>());
            }

            filmLikesDao.deleteAllLikesByFilm(film.getId());

            if (film.getLikes() != null) {
                film.getLikes().stream()
                        .forEach((like) -> {
                            genreFilmDao.addNewGenreInFilm(film.getId(), like);
                        });
            } else {
                film.setLikes(new HashSet<>());
            }
            directorFilmDao.deleteAllDirectorsByFilm(film.getId());

            if (film.getDirectors() != null) {
                film.getDirectors().stream()
                        .forEach((director) -> {
                            directorFilmDao.addNewDirectorInFilm(film.getId(), director.getId());
                        });
            } else {
                film.setDirectors(new LinkedHashSet<>());
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
    public List<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year) {
        return jdbcTemplate.query(GET_POPULAR_FILMS_SQL2, (rs, rowNum) -> buildFilm(rs), genreId, genreId, year, year
                , count);
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        return jdbcTemplate.query(GET_COMMON_FILMS_SQL, (rs, rowNum) -> buildFilm(rs), userId, friendId);
    }

    @Override
    public void addLikeToFilm(int filmId, int userId) {
        filmLikesDao.addLikeToFilm(filmId, userId);
        log.info(stringToGreenColor("The user={} add like to film={}"), userId, filmId);
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
        LinkedHashSet<Director> directors = new LinkedHashSet<>(directorFilmDao.getDirectorsByFilm(result.getId()));
        result.setLikes(usersLikes);
        result.setGenres(genres);
        result.setDirectors(directors);
        return result;
    }

    public Boolean checkFilmExist(Integer filmId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_EXIST_FILM_SQL, Integer.class, filmId);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<Film> getRecommendations(Integer id) {
        Set<Integer> recommendationFilmId = new HashSet<>();
        List<Film> recommendationFilm = new ArrayList<>();
        final List<Integer> userFilmsIDList = new ArrayList<>(getFilmsIDList(id));
        final String sqlQueryUsersID = "SELECT USERS_ID from USERS";
        final List<Integer> allUsersIDList = new ArrayList<>(jdbcTemplate.query(sqlQueryUsersID,
                                                                                this::getIdForUserList)); // собираем
        // строку с id всех Ю в лист
        allUsersIDList.remove(id); // удаляем самого себя из общего листа

        int crossListSize = 0;

        List<Integer> finalUserId = new ArrayList<>();

        for (Integer userId : allUsersIDList) {
            if (getCrossListFilmsId(userId, userFilmsIDList).size() > crossListSize) {
                crossListSize = getCrossListFilmsId(userId, userFilmsIDList).size();
            }
        }
        for (Integer userId : allUsersIDList) {
            if (getCrossListFilmsId(userId, userFilmsIDList).size() == crossListSize) {
                finalUserId.add(userId);
            }
        }
        for (Integer userId : finalUserId) {
            List<Integer> excludeUserFilmsIDList = new ArrayList<>(userFilmsIDList);
            final List<Integer> otherUserFilmsIDList = new ArrayList<>(getFilmsIDList(userId));
            otherUserFilmsIDList.removeAll(excludeUserFilmsIDList);
            recommendationFilmId.addAll(otherUserFilmsIDList);
        }
        for (Integer filmId : recommendationFilmId) {
            recommendationFilm.add(getFilmById(filmId));
        }
        return recommendationFilm;
    }

    private Integer getIdForUserList(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("Users_ID");
    }

    private List<Integer> getCrossListFilmsId(Integer userId, List<Integer> userFilmsIDList) { // получаем список
        // айди с пересечениями
        List<Integer> includeUserFilmsIDList = new ArrayList<>(userFilmsIDList); //
        final List<Integer> otherUserFilmsIDList = new ArrayList<>(getFilmsIDList(userId));
        includeUserFilmsIDList.retainAll(otherUserFilmsIDList);
        return includeUserFilmsIDList;
    }

    private List<Integer> getFilmsIDList(Integer userId) {
        final String sqlQueryFilmsID = "SELECT FILM_ID from FILM_LIKES where USERS_ID = ?";

        return new ArrayList<>(jdbcTemplate.query(sqlQueryFilmsID,
                                                  this::getFilmsIdForList, userId));
    }

    private Integer getFilmsIdForList(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("FILM_ID");
    }

    @Override
    public List<Film> searchFilmByDirector(String director) {
        String sqlQuery = SEARCH_FILMS_BY_DIRECTOR;
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> buildFilm(rs), "%" + director + "%");
    }

    @Override
    public List<Film> searchFilmByTitle(String title) {
        String sqlQuery = SEARCH_FILMS_BY_TITLE;
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> buildFilm(rs), "%" + title + "%");
    }

    @Override
    public List<Film> searchByTitleDirector(String dirtit) {
        String sqlQuery = SEARCH_FILMS_BY_TITLE_DIRECTOR;
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> buildFilm(rs), "%" + dirtit + "%", "%" + dirtit + "%");
    }

}