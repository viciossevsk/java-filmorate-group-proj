package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToBlueColor;
import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToGreenColor;


@RestController
@Slf4j
@RequestMapping("/directors")
public class DirectorController {
    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    private DirectorService directorService;

    /**
     * GET /directors - Список всех режиссёров
     */
    @GetMapping
    public List<Director> getAllDirectors() {
        log.info(stringToGreenColor("call method getAllDirectors... via GET /directors"));
        return directorService.getAllDirectors();
    }

    /**
     * GET /directors/{id}- Получение режиссёра по id
     */

    @GetMapping("/{id}")
    public Director getDirectorsById(@PathVariable("id") Integer directorId
    ) {
        log.info(stringToGreenColor("call method getDirectorsById... via GET /directors"));
        return directorService.getDirectorsById(directorId);
    }

    /**
     * POST /directors - Создание режиссёра
     */

    @PostMapping
    public Director createDirector(@Valid @RequestBody Director director) {
        log.info(stringToGreenColor("call method createDirector... via POST /directors"));
        log.info(stringToBlueColor(director.toString()));
        return directorService.createDirector(director);
    }

    /**
     * PUT /directors - Изменение режиссёра
     */
    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info(stringToGreenColor("call method updateDirector... via PUT /directors"));
        return directorService.updateDirector(director);
    }

    /**
     * DELETE /directors/{id} - Удаление режиссёра
     */

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable("id") Integer directorId) {
        log.info(stringToGreenColor("call method deleteDirector... via DELETE /directors"));
        directorService.deleteDirector(directorId);
    }
}