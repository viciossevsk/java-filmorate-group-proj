package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
public class GenreDaoImpl implements GenreDao {
    private final static String GET_ALL_GENRES_SQL = "select * from genre";
    private final static String GET_GENRE_BY_ID_SQL = "select * from genre where genre_id = ?";
    private final static String GET_ALL_GENRE_NAMES_SQL = "select name from genre";
    private final static String GET_GENRE_ID_BY_NAME_SQL = "select genre_id from genre where name = ?";
    private final static String SET_NEW_GENRE_SQL = "insert into genre (name) values(?)";
    private final static String CHECK_EXIST_GENRE_SQL = "select count(*) as cnt from genre where genre_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query(GET_ALL_GENRES_SQL, (rs, rowNum) -> buildGenre(rs));
    }

    @Override
    public List<String> getAllGenreNames() {
        return jdbcTemplate.query(GET_ALL_GENRE_NAMES_SQL, (rs, rowNum) -> buildGenreName(rs));
    }

    @Override
    public Genre getGenreById(Integer genreId) {
        if (checkGenreExist(genreId)) {
            Genre genre = jdbcTemplate.queryForObject(GET_GENRE_BY_ID_SQL, (rs, rowNum) -> buildGenre(rs), genreId);
            return genre;
        } else {
            throw new GenreNotFoundException("Genre with id=" + genreId + "not found");
        }
    }

    @Override
    public Integer getGenreIdByGenreName(String name) {
        Integer genreId = jdbcTemplate.queryForObject(GET_GENRE_ID_BY_NAME_SQL, (rs, rowNum) -> buildGenreId(rs), name);

        if (genreId == null) {
            throw new GenreNotFoundException("Genre with name=" + name + "not found");
        } else
            return genreId;
    }

    @Override
    public Integer addNewGenre(String name) {
        KeyHolder genreKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(SET_NEW_GENRE_SQL, new String[]{"genre_id"});
            stmt.setString(1, name);
            return stmt;
        }, genreKeyHolder);
        return Objects.requireNonNull(genreKeyHolder.getKey()).intValue();
    }

    private Genre buildGenre(ResultSet rs) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("genre_id"))
                .name(rs.getString("name"))
                .build();
    }

    private String buildGenreName(ResultSet rs) throws SQLException {
        return rs.getString("name");
    }

    private Integer buildGenreId(ResultSet rs) throws SQLException {
        return rs.getInt("genre_id");
    }

    private Boolean checkGenreExist(Integer genreId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_EXIST_GENRE_SQL, Integer.class, genreId);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

}
