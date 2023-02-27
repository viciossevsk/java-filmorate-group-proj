package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToGreenColor;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {

    private FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info(stringToGreenColor("call method getAllFilms... via GET /films"));
        return filmService.getAllFilms();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info(stringToGreenColor("call method add film... via POST /film"));
        return filmService.createFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info(stringToGreenColor("call method update film... via PUT /film"));
        return filmService.updateFilm(film);
    }

    /**
     * пользователь ставит лайк фильму
     *
     * @param id     фильма
     * @param userId - ИД юзера
     */
    @PutMapping("/{id}/like/{userId}")
    public void addLikeToFilm(@PathVariable("id") Integer filmId, @PathVariable Integer userId) {
        log.info(stringToGreenColor("call method add like film... via PUT /films"));
        filmService.addLikeToFilm(filmId, userId);
    }

    /**
     * пользователь удаляет лайк.
     *
     * @param id     фильма
     * @param userId - ИД юзера
     */
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeFromFilm(@PathVariable("id") Integer filmId, @PathVariable Integer userId) {
        log.info(stringToGreenColor("call remove like from film... via DELETE /films"));
        filmService.removeLikeFromFilm(filmId, userId);
    }

    /**
     * возвращает список из первых count фильмов по количеству лайков
     *
     * @param count - количество фильмов
     * @return Если значение параметра count не задано, верните первые 10
     */
    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        log.info(stringToGreenColor("call method getAllFilms... via GET /films"));
        return filmService.getMostPopularFilms(count);
    }


}
