package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewsController {

    private final ReviewService reviewService;

    @PostMapping()
    public Review addReview(@Valid @RequestBody Review review) {
        return reviewService.addReview(review);
    }

    @PutMapping()
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable("id") Integer id) {
        reviewService.removeReview(id);
    }

    @GetMapping("/{id}")
    public Review receiveReview(@PathVariable("id") Integer id) {
        return reviewService.receiveReview(id);
    }

    @GetMapping()
    public Collection<Review> receiveFilmsReviews(
            @RequestParam(defaultValue = "10", required = false) Integer count,
            @RequestParam(defaultValue = "0", required = false) String filmId) {
        if (filmId.equals("0")) {
            return reviewService.receiveAllReview();
        }
        return reviewService.receiveFilmsReviews(count, filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeReview(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        reviewService.likeReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        reviewService.dislikeReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeReview(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        reviewService.removeLikeReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislikeReview(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        reviewService.removeDislikeReview(id, userId);
    }
}
