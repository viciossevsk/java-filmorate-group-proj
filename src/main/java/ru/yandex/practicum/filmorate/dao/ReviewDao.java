package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.List;

public interface ReviewDao {
    Review addReview(Review review);

    Review updateReview(Review review);

    void removeReview(Integer id);

    Review receiveReview(Integer id);

    Collection<Review> receiveFilmsReviews(Integer count, String filmId);

    void likeReview(Integer id, Integer userId);

    void dislikeReview(Integer id, Integer userId);

    void removeLikeReview(Integer id, Integer userId);

    void removeDislikeReview(Integer id, Integer userId);

    List<Review> receiveAllReview();
}
