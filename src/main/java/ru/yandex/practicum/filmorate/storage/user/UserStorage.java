package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;

import java.util.List;

public interface UserStorage {

    List<User> getAllUsers();

    User createUser(User user);

    User updateUser(User user);

    List<UserEvent> getFeedByUserId(Integer userId);

    User getUserById(Integer userId);

    void addFriend(int userId, int friendId);

    void deleteFriend(int userId, int friendId);

    void deleteUserById(int userId);

}
