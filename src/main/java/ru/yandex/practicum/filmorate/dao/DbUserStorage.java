package ru.yandex.practicum.filmorate.dao;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.otherFunction.EventType;
import ru.yandex.practicum.filmorate.otherFunction.OperationType;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToGreenColor;

@Component
@Primary
@Slf4j
@Data
public class DbUserStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmLikesDao filmLikesDao;
    private final UserEventDao userEventDao;
    private final static String GET_ALL_USERS_SQL = "select * from users";
    private final static String CHECK_EXIST_USER_SQL = "select count(*) as cnt from users where users_id = ?";
    private final static String GET_USER_BY_ID_SQL = "select * from users where users_id = ?";
    private final static String GET_USER_FRIEND_IDS_SQL = "select f.friend_user_id AS users_id from friendship f " +
            "where f.user_id = ?";
    private final static String GET_COMMON_FRIENDS_SQL = "SELECT f.FRIEND_USER_ID FROM FRIENDSHIP f WHERE" +
            "f.USER_ID IN (?,?)" +
            "GROUP BY f.FRIEND_USER_ID" +
            "HAVING count(f.FRIEND_USER_ID) > 1";
    private final static String SET_NEW_USER_SQL = "insert into users (email, login, name, birthday) " +
            "values(?, ?, ?, ?)";
    private final static String SET_NEW_FRIENDSHIP_SQL = "insert into friendship (user_id, friend_user_id) VALUES (?," +
            " ?)";
    private final static String UPDATE_USER_SQL = "update users set " +
            "email = ?, login = ?, name = ?, birthday = ? " +
            "where users_id = ?";
    private final static String DELETE_FRIENDS_BY_USER_ID_SQL = "delete from friendship where user_id = ?";
    private final static String DELETE_FRIENDSHIP_SQL = "delete from friendship where user_id = ? " +
            "and friend_user_id = ?";
    private final static String DELETE_USER_BY_USER_ID = "delete from users where users_id = ?";

    @Override
    public List<User> getAllUsers() {
        return jdbcTemplate.query(GET_ALL_USERS_SQL, (rs, rowNum) -> buildUser(rs));
    }

    @Override
    public User createUser(User user) throws UserException {
        KeyHolder userKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(SET_NEW_USER_SQL, new String[]{"users_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, userKeyHolder);
        int userId = Objects.requireNonNull(userKeyHolder.getKey()).intValue();
        user.setId(userId);
        log.info(stringToGreenColor("The user was successfully ADDED: {}"), user);
        return user;
    }

    @Override
    public User updateUser(User user) throws UserException {
        if (checkUserExist(user.getId())) {
        jdbcTemplate.update(UPDATE_USER_SQL
                , user.getEmail()
                , user.getLogin()
                , user.getName()
                , Date.valueOf(user.getBirthday())
                , user.getId());

        jdbcTemplate.update(DELETE_FRIENDS_BY_USER_ID_SQL, user.getId());

        if (user.getFriends() != null) {
            user.getFriends().stream()
                    .forEach((friend) -> {
                        jdbcTemplate.update(SET_NEW_FRIENDSHIP_SQL, user.getId(), friend);
                    });
        }
            log.info(stringToGreenColor("The user was successfully UPDATED: {}"), user);
        return user;
        } else {
            throw new UserNotFoundException("User with id=" + user.getId() + " not found");
        }
    }

    @Override
    public void deleteUserById(int userId) {
        jdbcTemplate.update(DELETE_USER_BY_USER_ID, userId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        jdbcTemplate.update(DELETE_FRIENDSHIP_SQL, userId, friendId);
        userEventDao.setUserEvent(userId, EventType.FRIEND, OperationType.REMOVE, friendId);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if ((checkUserExist(userId)) && (checkUserExist(friendId))) {
            jdbcTemplate.update(SET_NEW_FRIENDSHIP_SQL, userId, friendId);
            log.info(stringToGreenColor("User={} friendship to user={}"), userId, friendId);
            userEventDao.setUserEvent(userId, EventType.FRIEND, OperationType.ADD, friendId);
        } else {
            throw new UserNotFoundException("User with id=" + userId + " or friend with id=" + friendId + " not found" +
                                                    " or not exist");
        }
    }

    @Override
    public User getUserById(Integer userId) throws UserException {
        if (checkUserExist(userId)) {
            User user = jdbcTemplate.queryForObject(GET_USER_BY_ID_SQL, (rs, rowNum) -> buildUser(rs), userId);
            log.debug(stringToGreenColor("call method getUserById: {}"), user);
            return user;
        } else {
            throw new UserNotFoundException("User with id=" + userId + " not found");
        }
    }

    @Override
    public List<UserEvent> getFeedByUserId(Integer userId) {
        if (checkUserExist(userId)) {
            return userEventDao.getFeed(userId);
        } else {
            throw new UserNotFoundException("User with id=" + userId + " not found");
        }
    }

    private User buildUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("users_id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();
        User result = User.builder()
                .id(id)
                .email(email)
                .login(login)
                .name(name)
                .birthday(birthday)
                .build();
        Set<Integer> usersFriends = new HashSet<>(getListUserFried(result.getId()));
        result.setFriends(usersFriends);
        return result;
    }

    private List<Integer> getListUserFried(int userId) {
        return jdbcTemplate.query(GET_USER_FRIEND_IDS_SQL, (rs, rowNum) -> makeUserId(rs),
                                  userId);
    }

    private Integer makeUserId(ResultSet rs) throws SQLException {
        return rs.getInt("users_id");
    }

    public Boolean checkUserExist(Integer userId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_EXIST_USER_SQL, Integer.class, userId);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }
}
