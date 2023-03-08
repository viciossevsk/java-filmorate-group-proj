package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.otherFunction.EventType;
import ru.yandex.practicum.filmorate.otherFunction.OperationType;

import java.util.List;

public interface UserEventDao {

    List<UserEvent> getFeed(Integer UserId);

    void setUserEvent(Integer userId, EventType eventType, OperationType operationType, Integer entityId);
}
