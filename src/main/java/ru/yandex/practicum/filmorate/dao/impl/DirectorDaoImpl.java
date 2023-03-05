package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
@Primary
@Slf4j
public class DirectorDaoImpl implements DirectorDao {
    private final static String CHECK_EXIST_DIRECTOR_SQL = "select count(*) as cnt from DIRECTOR where DIRECTOR_id = ?";
    private final static String GET_ALL_DIRECTORS_SQL = "SELECT * FROM DIRECTOR";
    private final static String GET_DIRECTOR_BY_ID_SQL = "select * from DIRECTOR where DIRECTOR_id = ?";
    private final static String SET_NEW_DIRECTOR_SQL = "insert into DIRECTOR (name) values(?)";
    private final static String UPDATE_DIRECTOR_SQL = "update DIRECTOR set " +
            "name = ? " +
            "where DIRECTOR_id = ?";
    private final static String DELETE_DIRECTOR_SQL = "DELETE from DIRECTOR where DIRECTOR_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DirectorDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getAllDirectors() {
        log.info("call method getAllDirectors from DirectorDaoImpl");
        return jdbcTemplate.query(GET_ALL_DIRECTORS_SQL, (rs, rowNum) -> buildDirector(rs));
    }

    @Override
    public Director getDirectorById(Integer directorId) throws DirectorNotFoundException {
        if (checkDirectorExist(directorId)) {
            Director director = jdbcTemplate.queryForObject(GET_DIRECTOR_BY_ID_SQL, (rs, rowNum) -> buildDirector(rs)
                    , directorId);
            return director;
        } else {
            throw new DirectorNotFoundException("Director with id=" + directorId + "not found");
        }
    }

    @Override
    public Director createDirector(Director director) {
        KeyHolder filmKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(SET_NEW_DIRECTOR_SQL, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, filmKeyHolder);
        Integer director_id = Objects.requireNonNull(filmKeyHolder.getKey()).intValue();
        director.setId(director_id);
        log.info("The following director was successfully added: {}", director);
        return director;
    }

    @Override
    public Director updateDirector(Director director) throws DirectorNotFoundException {
        if (checkDirectorExist(director.getId())) {
            jdbcTemplate.update(UPDATE_DIRECTOR_SQL
                    , director.getName(), director.getId());
        } else {
            throw new DirectorNotFoundException("Director with id=" + director.getId() + " not found");
        }
        log.info("The following director was successfully updated: {}", director);
        return director;
    }

    @Override
    public void deleteDirector(Integer directorId) throws DirectorNotFoundException {
        if (checkDirectorExist(directorId)) {
            jdbcTemplate.update(DELETE_DIRECTOR_SQL
                    , directorId);
        } else {
            throw new DirectorNotFoundException("Director with id=" + directorId + " not found");
        }
        log.info("The following director with id=" + directorId + " was successfully deleted");
    }

    private Director buildDirector(ResultSet rs) throws SQLException {
        return Director.builder()
                .id(rs.getInt("director_id"))
                .name(rs.getString("name"))
                .build();
    }

    public Boolean checkDirectorExist(Integer directorId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_EXIST_DIRECTOR_SQL, Integer.class, directorId);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }
}
