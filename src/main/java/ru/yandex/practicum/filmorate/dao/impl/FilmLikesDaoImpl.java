package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmLikesDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class FilmLikesDaoImpl implements FilmLikesDao {
    private final JdbcTemplate jdbcTemplate;
    private final static String GET_USER_LIKES_BY_FILM_ID_SQL = "select users_id from film_likes where film_id = ?";
    private final static String SET_NEW_LIKE_TO_FILM_SQL = "insert into film_likes(film_id, users_id) values(?, ?)";
    private final static String DELETE_LIKE_FROM_FILM_SQL = "delete from film_likes where film_id = ? and users_id = ?";
    private final static String DELETE_ALL_LIKES_OF_FILM_SQL = "delete from film_likes where film_id = ?";
    private final static String DELETE_ALL_LIKES_OF_USER_SQL = "delete from film_likes where users_id = ?";

    @Autowired
    public FilmLikesDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addLikeToFilm(Integer filmId, Integer userId) {
        jdbcTemplate.update(SET_NEW_LIKE_TO_FILM_SQL, filmId, userId);
    }

    @Override
    public void removeLikeFromFilm(Integer filmId, Integer userId) {
        jdbcTemplate.update(DELETE_LIKE_FROM_FILM_SQL, filmId, userId);
    }

    @Override
    public List<Integer> getUserLikesByFilm(Integer filmId) {
        return new ArrayList<>(jdbcTemplate.query(GET_USER_LIKES_BY_FILM_ID_SQL, (rs, rowNum) -> buildUserId(rs),
                                                  filmId));
    }

    @Override
    public void deleteAllLikesByFilm(Integer filmId) {
        jdbcTemplate.update(DELETE_ALL_LIKES_OF_FILM_SQL, filmId);
    }

    @Override
    public void deleteAllLikesOfUser(Integer userId) {
        jdbcTemplate.update(DELETE_ALL_LIKES_OF_USER_SQL, userId);
    }

    private Integer buildUserId(ResultSet rs) throws SQLException {
        return rs.getInt("users_id");
    }
}
