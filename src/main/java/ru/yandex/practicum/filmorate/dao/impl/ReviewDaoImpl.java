package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DbFilmStorage;
import ru.yandex.practicum.filmorate.dao.DbUserStorage;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.dao.UserEventDao;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.RatingNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.otherFunction.EventType;
import ru.yandex.practicum.filmorate.otherFunction.OperationType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToGreenColor;

@RequiredArgsConstructor
@Component
@Slf4j
public class ReviewDaoImpl implements ReviewDao {

    private final JdbcTemplate jdbcTemplate;
    private final DbUserStorage dbUserStorage;
    private final DbFilmStorage dbFilmStorage;
    private final UserEventDao userEventDao;

    private final static String CHECK_EXIST_REVIEW_SQL = "select count(*) as cnt from REVIEW where REVIEW_ID = ?";
    private final static String GET_REVIEW_BY_ID_SQL = "select * from REVIEW where REVIEW_ID = ?";
    private final static String DELETE_REVIEW_BY_ID_SQL = "delete from review where review_id = ?";
    private final static String UPDATE_REVIEW_BY_ID_SQL = "UPDATE review " +
            "SET is_positive = ?, " +
            "content = ? " +
            "WHERE review_id = ?; ";
    private final static String GET_RECEIVE_FILMS_REVIEW_BY_ID_SQL = "select * from review where film_id = ?";
    private final static String GET_ALL_REVIEW_SQL = "select * from review";
    private final static String SET_LIKE_OR_DISLIKE_REVIEW_SQL = "INSERT INTO review_like (" +
            "review_id, " +
            "users_id, " +
            "like_or_dislike) " +
            "values" +
            "(?, ?, ?)";
    private final static String GET_REVIEW_USEFUL_SQL = "SELECT SUM(like_or_dislike) AS summary " +
            "FROM REVIEW_LIKE WHERE review_id = ?";
    private final static String DELETE_LIKE_OR_DISLIKE_REVIEW_SQL = "DELETE from review_like " +
            "WHERE review_id = ? AND users_id = ?";

    @Override
    public Review addReview(Review review) {
        if (dbFilmStorage.checkFilmExist(review.getFilmId()) && dbUserStorage.checkUserExist(review.getUserId())) {
            String sql = "INSERT INTO review (content, " + "        is_positive, " + "        users_id, " + "        "
                    + "film_id) values " + "(?, ?, ?, ?);";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
                ps.setString(1, review.getContent());
                ps.setBoolean(2, review.getIsPositive());
                ps.setInt(3, review.getUserId());
                ps.setInt(4, review.getFilmId());
                return ps;
            }, keyHolder);
            int reviewId = Objects.requireNonNull(keyHolder.getKey()).intValue();
            review.setId(reviewId);
            log.info(stringToGreenColor("The review was successfully ADDED: {}"), review);
            userEventDao.setUserEvent(review.getUserId(), EventType.REVIEW, OperationType.ADD, reviewId);
            return review;
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
        jdbcTemplate.update(UPDATE_REVIEW_BY_ID_SQL, review.getIsPositive(), review.getContent(), review.getId());
        Review change_review = getReviewById(review.getId());
        userEventDao.setUserEvent(change_review.getUserId(), EventType.REVIEW, OperationType.UPDATE,
                                  change_review.getId());
        return change_review;
    }

    @Override
    public void removeReview(Integer reviewId) {
        if (checkReviewExist(reviewId)) {
            Review review = getReviewById(reviewId);
            jdbcTemplate.update(DELETE_REVIEW_BY_ID_SQL, reviewId);
            userEventDao.setUserEvent(review.getUserId(), EventType.REVIEW, OperationType.REMOVE, review.getId());
        }
    }

    @Override
    public Collection<Review> receiveFilmsReviews(Integer count, String filmId) {
        return jdbcTemplate.query(GET_RECEIVE_FILMS_REVIEW_BY_ID_SQL, (rs, rowNum) -> makeReview(rs), filmId).stream().limit(count).sorted(Comparator.comparingInt(Review::getUseful).reversed()).collect(Collectors.toList());
    }

    @Override
    public List<Review> receiveAllReview() {
        List<Review> reviews = jdbcTemplate.query(GET_ALL_REVIEW_SQL, (rs, rowNum) -> makeReview(rs));
        reviews.sort(Comparator.comparingInt(Review::getUseful).reversed());
        return reviews;
    }

    @Override
    public void likeReview(Integer reviewId, Integer userId) {
        jdbcTemplate.update(SET_LIKE_OR_DISLIKE_REVIEW_SQL, reviewId, userId, 1);
        Review review = getReviewById(reviewId);
    }

    @Override
    public void dislikeReview(Integer reviewId, Integer userId) {
        jdbcTemplate.update(SET_LIKE_OR_DISLIKE_REVIEW_SQL, reviewId, userId, -1);
    }

    @Override
    public void removeLikeReview(Integer reviewId, Integer userId) {
        jdbcTemplate.update(DELETE_LIKE_OR_DISLIKE_REVIEW_SQL, reviewId, userId);
    }

    @Override
    public void removeDislikeReview(Integer reviewId, Integer userId) {
        jdbcTemplate.update(DELETE_LIKE_OR_DISLIKE_REVIEW_SQL, reviewId, userId);
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        int reviewId = rs.getInt("review_id");
        String content = rs.getString("content");
        boolean isPositive = rs.getBoolean("is_positive");
        int userId = rs.getInt("users_id");
        int filmId = rs.getInt("film_id");
        int useful = getReviewUseful(reviewId);
        return Review.builder().id(reviewId).content(content).isPositive(isPositive).userId(userId).filmId(filmId).useful(useful).build();
    }

    private int getReviewUseful(int reviewId) {
        SqlRowSet likesReviewRows =
                jdbcTemplate.queryForRowSet(GET_REVIEW_USEFUL_SQL, reviewId);
        if (likesReviewRows.next()) {
            return likesReviewRows.getInt("summary");
        }
        return 0;
    }

    @Override
    public Review getReviewById(Integer reviewId) {
        if (checkReviewExist(reviewId)) {
            Review review = jdbcTemplate.queryForObject(GET_REVIEW_BY_ID_SQL, (rs, rowNum) -> makeReview(rs),
                                                        reviewId);
            return review;
        } else {
            throw new RatingNotFoundException("Review with id=" + reviewId + " not found");
        }
    }

    private Boolean checkReviewExist(Integer reviewId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_EXIST_REVIEW_SQL, Integer.class, reviewId);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }
}