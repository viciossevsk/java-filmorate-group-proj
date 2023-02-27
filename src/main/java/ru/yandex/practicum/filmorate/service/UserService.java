package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToGreenColor;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    private final FilmStorage filmStorage;

    public User getUser(Integer id) {
        log.info(stringToGreenColor("call method getUser in UserStorage... via GET /users"));
        return userStorage.getUserById(id);
    }

    public List<User> getAllUsers() {
        log.info(stringToGreenColor("call method getAllUsers in UserStorage... via GET /users"));
        return userStorage.getAllUsers();
    }
    public User createUser(User user) {
        log.info(stringToGreenColor("call method add user in UserStorage... via POST /users"));
        validateUser(user);
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName((user.getLogin()));
        }
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        log.info(stringToGreenColor("call method update user in UserStorage... via PUT /users"));
        User userExist = userStorage.getUserById(user.getId());
        validateUser(user);
        return userStorage.updateUser(user);
    }

    public void addFriend(Integer UserId, Integer friendId) {
        if (UserId < 1) {
            throw new UserNotFoundException("User with id " + UserId + " not found");
        } else if (friendId < 1) {
            throw new UserNotFoundException("friend with id " + friendId + " not found");
        } else {
            userStorage.addFriend(UserId, friendId);
        }
    }

    public void deleteFriend(Integer UserId, Integer friendId) {
        userStorage.deleteFriend(UserId, friendId);

    }

    /**
     * получаем ИД друзей юзера
     * по ИД друга получаем объект
     * объединяем объект класса User в коллекцию
     * <p>
     * //        List<User> friends = new ArrayList<>();
     * //        for (Integer friendId : user.getFriends()){
     * //           User friend = userStorage.getUser(friendId);
     * //           friends.add(friend);
     * //        }
     */
    public List<User> getFriendsUser(Integer id) {
        User user = userStorage.getUserById(id);

        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    /**
     * получаем ИД друзей юзера
     * ИД друга ищем в друзьях у Other (находим только одинаковых друзей у обоих юзеров)
     * по ИД друга получаем объект
     * объединяем объект класса User в коллекцию
     */
    public List<User> getCommonFriends(Integer id, Integer otherId) {
        User user = userStorage.getUserById(id);
        User otherUser = userStorage.getUserById(otherId);

        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    private boolean validateUser(User user) throws ValidationException {
        if (user.getLogin().trim().isEmpty()) {
            throw new ValidationException("Login is empty");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Birthday must not be a date in future");
        }
        return true;
    }


}
