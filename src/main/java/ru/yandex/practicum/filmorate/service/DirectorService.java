package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToGreenColor;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorDao directorDao;

    public List<Director> getAllDirectors() {
        log.info(stringToGreenColor("call method getAllDirectors in directorDao"));
        return directorDao.getAllDirectors();
    }


    public Director getDirectorsById(Integer directorId) {
        log.info(stringToGreenColor("call method getDirectorsById in directorDao"));
        return directorDao.getDirectorById(directorId);
    }

    public Director createDirector(Director director) {
        log.info(stringToGreenColor("call method createDirector in directorDao"));
        validateDirector(director);
        return directorDao.createDirector(director);
    }

    public Director updateDirector(Director director) {
        log.info(stringToGreenColor("call method updateDirector in directorDao"));
        return directorDao.updateDirector(director);
    }

    public void deleteDirector(Integer directorId) {
        log.info(stringToGreenColor("call method deleteDirector in directorDao"));
        directorDao.deleteDirector(directorId);
    }

    private boolean validateDirector(Director director) throws ValidationException {
        if (director.getName().trim().isEmpty()) {
            throw new ValidationException("Director name cannot be empty");
        }
        return true;
    }
}


