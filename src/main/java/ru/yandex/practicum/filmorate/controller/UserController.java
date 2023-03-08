package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /***
     * показать всех юзеров
     */
    @GetMapping
    public List<User> getAllUsers() {
        //   log.info(stringToGreenColor("call method getAllUsers... via GET /users"));
        return userService.getAllUsers();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    /***
     * получаем юзера по ИД
     */
    @GetMapping("/{id}")
    public User getUser(@PathVariable Integer id) {
        return userService.getUser(id);
    }

    /**
     * добавление в друзья.
     *
     * @param id       - к кому добавляем
     * @param friendId - кого добавляем
     */
    @PutMapping(value = "/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) throws SQLException {
        userService.addFriend(id, friendId);
    }

    /**
     * удалить из друзей.
     *
     * @param id       - у кого удаляем друга
     * @param friendId - кого удаляем
     */
    @DeleteMapping(value = "/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.deleteFriend(id, friendId);
    }

    /**
     * возвращаем список пользователей, являющихся его друзьями
     */
    @GetMapping(value = "/{id}/friends")
    public List<User> getFriendsUser(@PathVariable Integer id) {
        return userService.getFriendsUser(id);
    }

    /**
     * список друзей, общих с другим пользователем.
     */
    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @DeleteMapping("{userId}")
    public void deleteUserById(@PathVariable("userId") Integer userId) {
        userService.deleteUserById(userId);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable Integer id) {
        return userService.getFilmStorage().getRecommendations(id);
    }


    @GetMapping("/{id}/feed")
    public List<UserEvent> getFeed(@PathVariable("id") Integer userid) {
        return userService.getFeed(userid);
    }

}
