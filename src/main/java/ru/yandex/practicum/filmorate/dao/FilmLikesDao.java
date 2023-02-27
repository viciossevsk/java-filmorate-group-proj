package ru.yandex.practicum.filmorate.dao;

import java.util.List;

public interface FilmLikesDao {
    void addLikeToFilm(Integer filmId, Integer userId);

    void removeLikeFromFilm(Integer filmId, Integer userId);

    List<Integer> getUserLikesByFilm(Integer filmId);

    void deleteAllLikesByFilm(Integer filmId);

    void deleteAllLikesOfUser(Integer userId);
}
