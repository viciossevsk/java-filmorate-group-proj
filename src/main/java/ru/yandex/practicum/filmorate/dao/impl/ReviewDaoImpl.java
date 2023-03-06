package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DbFilmStorage;
import ru.yandex.practicum.filmorate.dao.DbUserStorage;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ReviewDaoImpl implements ReviewDao {

    private final JdbcTemplate jdbcTemplate;
    private final DbUserStorage dbUserStorage;
    private final DbFilmStorage dbFilmStorage;


    @Override
    public Review addReview(Review review) {
        if (dbFilmStorage.checkFilmExist(review.getFilmId()) && dbUserStorage.checkUserExist(review.getUserId())) {
            String sql = "INSERT INTO review (content, " +
                    "        is_positive, " +
                    "        users_id, " +
                    "        film_id) values " +
                    "(?, ?, ?, ?);";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
                ps.setString(1, review.getContent());
                ps.setBoolean(2, review.getIsPositive());
                ps.setInt(3, review.getUserId());
                ps.setInt(4, review.getFilmId());
                return ps;
            }, keyHolder);
            int reviewId = Objects
                    .requireNonNull(keyHolder.getKey())
                    .intValue();
            return receiveReview(reviewId);
        } else if (review.getUserId() == null) {
            throw new ValidationException("User has to exist");
        } else if (review.getFilmId() == null) {
            throw new ValidationException("Film has to exist");
        } else if (!dbUserStorage.checkUserExist(review.getUserId())) {
            throw new UserNotFoundException("User with id=" + review.getUserId() + " not found");
        } else if (!dbFilmStorage.checkFilmExist(review.getFilmId())) {
            throw new FilmNotFoundException("Film with id=" + review.getFilmId() + " not found");
        }
        return null;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE review SET is_positive = ?, content = ? " +
                "WHERE review_id = ?; ";
        jdbcTemplate.update(sql,
                review.getIsPositive(),
                review.getContent(),
                review.getId());
        return receiveReview(review.getId());
    }

    @Override
    public void removeReview(Integer id) {
        String sql = "delete from review where review_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Review receiveReview(Integer id) {
        String sql = "select review_id, content, is_positive, users_id, film_id " +
                "from review " +
                "WHERE review_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ReviewException("Запрашиваемый отзыв отсутствует в базе"));
    }

    @Override
    public Collection<Review> receiveFilmsReviews(Integer count, String filmId) {
        String sql = "select review_id, content, is_positive, users_id, film_id " +
                "from review " +
                "where film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), filmId).stream()
                .limit(count)
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> receiveAllReview() {
        String sql = "select review_id, content, is_positive, users_id, film_id " +
                "from review ";
        List<Review> reviews = jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs));
        reviews.sort(Comparator.comparingInt(Review::getUseful).reversed());
        return reviews;
    }


    @Override
    public void likeReview(Integer id, Integer userId) {
        String sql = "INSERT INTO review_like (review_id, " +
                "        users_id, " +
                "        like_or_dislike) values " +
                "(?, ?, ?);";
        jdbcTemplate.update(sql,
                id,
                userId,
                1);
    }

    @Override
    public void dislikeReview(Integer id, Integer userId) {
        String sql = "INSERT INTO review_like (review_id, " +
                "        users_id, " +
                "        like_or_dislike) values " +
                "(?, ?, ?);";
        jdbcTemplate.update(sql,
                id,
                userId,
                -1);
    }

    @Override
    public void removeLikeReview(Integer id, Integer userId) {
        String sql = "DELETE from review_like " +
                "WHERE review_id = ? AND users_id = ?; ";
        jdbcTemplate.update(sql,
                id,
                userId);
    }

    @Override
    public void removeDislikeReview(Integer id, Integer userId) {
        String sql = "DELETE from review_like " +
                "WHERE review_id = ? AND users_id = ?;";
        jdbcTemplate.update(sql,
                id,
                userId);
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        int id = rs.getInt("review_id");
        String content = rs.getString("content");
        boolean isPositive = rs.getBoolean("is_positive");
        int userId = rs.getInt("users_id");
        int filmId = rs.getInt("film_id");
        int useful = getReviewUseful(id);
        return Review.builder()
                .id(id)
                .content(content)
                .isPositive(isPositive)
                .userId(userId)
                .filmId(filmId)
                .useful(useful)
                .build();
    }

    private int getReviewUseful(int id) {
        SqlRowSet likesReviewRows = jdbcTemplate.queryForRowSet("SELECT SUM(like_or_dislike) AS summary " +
                "FROM REVIEW_LIKE " +
                "WHERE review_id = ?", id);
        if (likesReviewRows.next()) {
            return likesReviewRows.getInt("summary");
        }
        return 0;
    }
}
