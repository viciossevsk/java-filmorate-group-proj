package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.RatingDao;
import ru.yandex.practicum.filmorate.exception.RatingNotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
public class RatingDaoImpl implements RatingDao {
    private final static String GET_ALL_RATINGS_SQL = "select * from rating";
    private final static String GET_RATING_BY_ID_SQL = "select * from rating where rating_id = ?";
    private final static String GET_RATING_ID_BY_NAME_SQL = "select rating_id from rating where name = ?";
    private final static String SET_NEW_RATING_SQL = "insert into rating (name) values(?)";
    private final static String CHECK_EXIST_RATING_SQL = "select count(*) as cnt from rating where rating_id = ?";
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RatingDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Integer addNewRating(String name) {
        KeyHolder ratingKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(SET_NEW_RATING_SQL, new String[]{"rating_id"});
            stmt.setString(1, name);
            return stmt;
        }, ratingKeyHolder);
        return Objects.requireNonNull(ratingKeyHolder.getKey()).intValue();
    }

    @Override
    public Integer getRatingIdForRatingName(String name) {
        Integer ratingId = jdbcTemplate.queryForObject(GET_RATING_ID_BY_NAME_SQL, (rs, rowNum) -> buildRatingId(rs),
                                                       name);

        if (ratingId == null) {
            throw new RatingNotFoundException("Rating with name=" + name + "not found");
        } else
            return ratingId;
    }

    @Override
    public List<Rating> getAllRatings() {
        return jdbcTemplate.query(GET_ALL_RATINGS_SQL, (rs, rowNum) -> buildRating(rs));
    }

    @Override
    public Rating getRatingById(Integer ratingId) {
        if (checkRatingExist(ratingId)) {
            Rating rating = jdbcTemplate.queryForObject(GET_RATING_BY_ID_SQL, (rs, rowNum) -> buildRating(rs),
                                                        ratingId);
            return rating;
        } else {
            throw new RatingNotFoundException("Rating with id=" + ratingId + "not found");
        }

    }

    private Rating buildRating(ResultSet rs) throws SQLException {
        return Rating.builder()
                .id(rs.getInt("rating_id"))
                .name(rs.getString("name"))
                .build();
    }

    private Integer buildRatingId(ResultSet rs) throws SQLException {
        return rs.getInt("rating_id");
    }

    private Boolean checkRatingExist(Integer ratingId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_EXIST_RATING_SQL, Integer.class, ratingId);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

}
