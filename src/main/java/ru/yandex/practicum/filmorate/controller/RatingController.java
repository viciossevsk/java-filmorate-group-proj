package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/mpa")
public class RatingController {

    private final FilmService filmService;

    public RatingController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Rating> getAllRatings() {
        return filmService.getAllRatings();
    }

    @GetMapping("/{id}")
    public Rating getRatingById(@PathVariable("id") Integer ratingId) {
        return filmService.getRatingById(ratingId);
    }


}
