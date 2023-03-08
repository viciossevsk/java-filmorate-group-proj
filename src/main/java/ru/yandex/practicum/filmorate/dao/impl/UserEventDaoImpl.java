package ru.yandex.practicum.filmorate.dao.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserEventDao;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.otherFunction.EventType;
import ru.yandex.practicum.filmorate.otherFunction.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static ru.yandex.practicum.filmorate.otherFunction.AddvansedFunctions.stringToRedColor;

@Component
@Data
@Slf4j
public class UserEventDaoImpl implements UserEventDao {
    private final JdbcTemplate jdbcTemplate;
    
    private final static String SET_EVENT_TO_USER_SQL = "INSERT INTO USER_EVENT " +
            "(USERS_ID, EVENT_TYPE, OPERATION_TYPE, ENTITY_ID) " +
            "VALUES " +
            "(?, ?, ?, ?)";
    private final static String GET_EVENTS_TO_USER_SQL = "SELECT * FROM USER_EVENT " +
            "WHERE USERS_ID = ? ";

    @Override
    public List<UserEvent> getFeed(Integer userId) {
        return jdbcTemplate.query(GET_EVENTS_TO_USER_SQL, (rs, rowNum) -> buildUserEvent(rs), userId);
    }

    private UserEvent buildUserEvent(ResultSet rs) throws SQLException {
        long timestamp = rs.getTimestamp("TIME_STAMP").toInstant().toEpochMilli();
        return UserEvent.builder()
                .timestamp(timestamp)
                .userId(rs.getInt("USERS_ID"))
                .eventType(rs.getString("EVENT_TYPE"))
                .operationType(rs.getString("OPERATION_TYPE"))
                .entityId(rs.getInt("ENTITY_ID"))
                .id(rs.getInt("USER_EVENT_ID"))
                .build();
    }

    @Override
    public void setUserEvent(Integer userId, EventType eventType, OperationType operationType, Integer entityId) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.update(SET_EVENT_TO_USER_SQL,  userId, eventType.toString(), operationType.toString(),
                            entityId);
        log.info(stringToRedColor("Create feed timestamp={}, userId={}, event={}, operation={}, entityId={}"),
                 timestamp, userId, eventType.toString(), operationType.toString(), entityId);
    }
}
