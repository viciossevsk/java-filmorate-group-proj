package ru.yandex.practicum.filmorate;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateDbApplicationTests {
    private final UserService userService;
    private final FilmService filmService;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        User user1 = userService.createUser(User.builder()
                                                    .email("qwer@ui.ru")
                                                    .login("qwerty")
                                                    .name("pavel")
                                                    .birthday(LocalDate.of(1990, 5, 6))
                                                    .build());

        User user2 = userService.createUser(User.builder()
                                                    .email("qwer2@ui.ru")
                                                    .login("qwerty2")
                                                    .name("pavel2")
                                                    .birthday(LocalDate.of(1990, 5, 6))
                                                    .build());

        User user3 = userService.createUser(User.builder()
                                                    .email("qwer3@ui.ru")
                                                    .login("qwerty3")
                                                    .name("pavel3")
                                                    .birthday(LocalDate.of(1990, 5, 6))
                                                    .build());
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();

        genres.add(Genre.builder().id(1).build());

        Film film1 = filmService.createFilm(Film.builder()
                                                    .name("Friends")
                                                    .likes(new HashSet<>())
                                                    .description("sitcom about friends")
                                                    .releaseDate(LocalDate.of(1994, 10, 26))
                                                    .duration(108)
                                                    .mpa(Rating.builder().id(1).build())
                                                    .genres(genres)
                                                    .build());

        LinkedHashSet<Genre> genres2 = new LinkedHashSet<>();

        genres2.add(Genre.builder().id(2).build());

        Film film2 = filmService.createFilm(Film.builder()
                                                    .name("Clinic")
                                                    .likes(new HashSet<>())
                                                    .description("sitcom about doctors")
                                                    .releaseDate(LocalDate.of(2001, 01, 01))
                                                    .duration(109)
                                                    .mpa(Rating.builder().id(1).build())
                                                    .genres(genres2)
                                                    .build());

//        userService.addFriend(user1.getId(), user2.getId());
//        userService.addFriend(user2.getId(), user3.getId());
//        userService.addFriend(user3.getId(), user2.getId());
//        filmService.addLikeToFilm(film1.getId(), user1.getId());
//        filmService.addLikeToFilm(film1.getId(), user2.getId());
//        filmService.addLikeToFilm(film2.getId(), user3.getId());
    }

    @AfterEach
    void afterEach() {
        jdbcTemplate.update("DELETE FROM FRIENDSHIP");
        jdbcTemplate.update("DELETE FROM FILM_LIKES");
        jdbcTemplate.update("DELETE FROM GENRE_FILM");
        jdbcTemplate.update("ALTER TABLE FRIENDSHIP ALTER COLUMN FRIENDSHIP_ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE FILM_LIKES ALTER COLUMN FILM_LIKES_ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE GENRE_FILM ALTER COLUMN GENRE_FILM_ID RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM USERS");
        jdbcTemplate.update("DELETE FROM FILM");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN USERS_ID RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE FILM ALTER COLUMN FILM_ID RESTART WITH 1");
    }

    private int getFilmIdByName(String filmName) {
        return filmService.getAllFilms().stream()
                .filter(f -> f.getName().equals(filmName))
                .findFirst().get().getId();
    }

    private int getUserIdByName(String userName) {
        return userService.getAllUsers().stream()
                .filter(u -> u.getName().equals(userName))
                .findFirst().get().getId();
    }

    //    @Test
//    void addLike() {
//        Assertions.assertEquals(2, filmService.getFilmById(getFilmIdByName("Friends"))
//                .takeCountLikes());
//        Assertions.assertEquals(1, filmService.getFilmById(getFilmIdByName("Clinic"))
//                .takeCountLikes());
//    }
    @Test
    void getAllFilms() {
        Assertions.assertEquals(2, filmService.getAllFilms().size());
    }

    //
//    @Test
//    void addToFriends() {
//        Assertions.assertEquals(1, userService.getFriendsUser(getUserIdByName("pavel")).size());
//        Assertions.assertEquals(1, userService.getFriendsUser(getUserIdByName("pavel2")).size());
//    }
//
    @Test
    void getUserById() {
        Assertions.assertEquals(getUserIdByName("pavel"),
                                userService.getUser(getUserIdByName("pavel")).getId());
    }

}
