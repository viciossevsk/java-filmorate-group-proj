package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreFilmDao;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class GenreFilmDaoImpl implements GenreFilmDao {
    private final JdbcTemplate jdbcTemplate;
    private final static String GET_GENRES_BY_FILM_SQL = "select * from genre g " +
            "join GENRE_FILM gf ON gf.GENRE_ID = g.GENRE_ID  where film_id = ?";
    private final static String SET_NEW_GENRE_FOR_FILM_SQL = "insert into genre_film (film_id, genre_id) values(?, ?)";
    private final static String DELETE_GENRE_FILM_SQL = "delete from genre_film where film_id = ? and genre_id = ?";
    private final static String DELETE_GENRE_FILM_BY_FILM_SQL = "delete from genre_film where film_id = ?";

    @Autowired
    public GenreFilmDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addNewGenreInFilm(Integer filmId, Integer genreId) {
        jdbcTemplate.update(SET_NEW_GENRE_FOR_FILM_SQL, filmId, genreId);
    }

    @Override
    public List<Genre> getGenresByFilm(Integer filmId) {
        return jdbcTemplate.query(GET_GENRES_BY_FILM_SQL, (rs, rowNum) -> makeGenre(rs), filmId);
    }

    @Override
    public void deleteGenreFilm(Integer filmId, Integer genreId) {
        jdbcTemplate.update(DELETE_GENRE_FILM_SQL, filmId, genreId);
    }

    @Override
    public void deleteAllGenreByFilm(Integer filmId) {
        jdbcTemplate.update(DELETE_GENRE_FILM_BY_FILM_SQL, filmId);
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("genre_id"))
                .name(rs.getString("name"))
                .build();
    }
}
