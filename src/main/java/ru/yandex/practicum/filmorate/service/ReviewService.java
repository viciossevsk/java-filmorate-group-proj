package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.ReviewDaoImpl;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewDaoImpl reviewDao;

    public Review addReview(Review review) {
        return reviewDao.addReview(review);
    }

    public Review updateReview(Review review) {
        return reviewDao.updateReview(review);
    }

    public void removeReview(Integer id) {
        reviewDao.removeReview(id);
    }

    public Review receiveReview(Integer id) {
        return reviewDao.receiveReview(id);
    }

    public Collection<Review> receiveFilmsReviews(Integer count, String filmId) {
        return reviewDao.receiveFilmsReviews(count, filmId);
    }

    public void likeReview(Integer id, Integer userId) {
        reviewDao.likeReview(id, userId);
    }

    public void dislikeReview(Integer id, Integer userId) {
        reviewDao.dislikeReview(id, userId);
    }

    public void removeLikeReview(Integer id, Integer userId) {
        reviewDao.removeLikeReview(id, userId);
    }

    public void removeDislikeReview(Integer id, Integer userId) {
        reviewDao.removeDislikeReview(id, userId);
    }

    public List<Review> receiveAllReview() {
        return reviewDao.receiveAllReview();
    }
}
