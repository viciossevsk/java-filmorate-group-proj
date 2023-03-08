package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToGreenColor;
import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToRedColor;

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
        return filmService.getAllFilms();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable("id") Integer filmId) {
        return filmService.getFilmById(filmId);
    }

    @GetMapping("/director/{id}")
    public List<Film> getFilmsDirectorsSortBy(@PathVariable("id") Integer directorId,
                                              @RequestParam(defaultValue = "name", required = false) String sortBy) {
        return filmService.getFilmsDirectorsSortBy(directorId, sortBy);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info(stringToGreenColor("call method update film... via PUT /film"));
        return filmService.updateFilm(film);
    }

    /**
     * пользователь ставит лайк фильму
     *
     * @param filmId     фильма
     * @param userId - ИД юзера
     */
    @PutMapping("/{id}/like/{userId}")
    public void addLikeToFilm(@PathVariable("id") Integer filmId, @PathVariable Integer userId) {
        filmService.addLikeToFilm(filmId, userId);
    }

    /**
     * пользователь удаляет лайк.
     *
     * @param filmId     фильма
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
     * @param genreId - фильтр по жанру фильма
     * @param year - фильтр по году выпуска фильма
     * @return Если значение параметра count не задано, верните первые 10
     */
    @GetMapping("/popular")
    public List<Film> getMostPopularFilmsWithGenreYear(@RequestParam(defaultValue = "10", required = false) Integer count,
                                                       @RequestParam(required = false) Integer genreId,
                                                       @RequestParam(required = false) Integer year) {
        log.info(stringToGreenColor("call method getMostPopularFilms... via GET /films"));
        return filmService.getMostPopularFilms(count, genreId, year);
    }

    @DeleteMapping("{filmId}")
    public void deleteFilmById(@PathVariable("filmId") Integer filmId) {
        log.info(stringToRedColor("call remove film by filmId... via DELETE /films"));
        filmService.deleteFilmById(filmId);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam Integer userId,
                                     @RequestParam Integer friendId) {
        log.info(stringToGreenColor("call method getCommonFilms... via GET /films"));
        return filmService.getCommonFilms(userId, friendId);
    }
}
