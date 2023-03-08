package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorFilmDao;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class DirectorFilmDaoImpl implements DirectorFilmDao {
    private final JdbcTemplate jdbcTemplate;
    private final static String DELETE_ALL_DIRECTORS_OF_FILM_SQL = "delete from DIRECTOR_FILM where film_id = ?";
    private final static String SET_NEW_DIRECTOR_FOR_FILM_SQL = "insert into DIRECTOR_FILM (film_id, director_id) " +
            "values(?, ?)";
    private final static String GET_DIRECTOR_BY_FILM_SQL = "select * from DIRECTOR d " +
            "join DIRECTOR_FILM df ON df.director_id = d.director_id  where film_id = ?";

    @Autowired
    public DirectorFilmDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void deleteAllDirectorsByFilm(Integer filmId) {
        jdbcTemplate.update(DELETE_ALL_DIRECTORS_OF_FILM_SQL, filmId);
    }

    @Override
    public List<Director> getDirectorsByFilm(Integer filmId) {
        return jdbcTemplate.query(GET_DIRECTOR_BY_FILM_SQL, (rs, rowNum) -> buildDirector(rs), filmId);
    }

    @Override
    public void addNewDirectorInFilm(Integer filmId, Integer directorId) {
        jdbcTemplate.update(SET_NEW_DIRECTOR_FOR_FILM_SQL, filmId, directorId);
    }

    private Director buildDirector(ResultSet rs) throws SQLException {
        return Director.builder()
                .id(rs.getInt("director_id"))
                .name(rs.getString("name"))
                .build();
    }
}
