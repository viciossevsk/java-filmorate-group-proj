drop table  IF EXISTS PUBLIC.GENRE CASCADE;
drop table  IF EXISTS PUBLIC.RATING CASCADE;
drop table  IF EXISTS PUBLIC.FILM CASCADE;
drop table  IF EXISTS PUBLIC.GENRE_FILM CASCADE;
drop table  IF EXISTS PUBLIC.USERS CASCADE;
drop table  IF EXISTS PUBLIC.FILM_LIKES CASCADE;
drop table  IF EXISTS PUBLIC.FRIENDSHIP CASCADE;
drop table  IF EXISTS PUBLIC.REVIEW CASCADE;
drop table  IF EXISTS PUBLIC.REVIEW_LIKE CASCADE;
drop table  IF EXISTS PUBLIC.DIRECTOR CASCADE;
drop table  IF EXISTS PUBLIC.USER_EVENT CASCADE;


CREATE TABLE IF NOT EXISTS PUBLIC.GENRE
(
    GENRE_ID
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    NAME
    VARCHAR
(
    255
) NOT NULL
    );

create TABLE IF NOT EXISTS PUBLIC.RATING
(
    RATING_ID
        INTEGER
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    NAME
        VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS PUBLIC.DIRECTOR
(
    DIRECTOR_ID
        INTEGER
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    NAME
        VARCHAR(255) NOT NULL
);

create TABLE IF NOT EXISTS PUBLIC.FILM
(
    FILM_ID
    INTEGER
    GENERATED
    BY
        DEFAULT AS
        IDENTITY
        PRIMARY
            KEY,
    NAME
    VARCHAR(255)             NOT NULL,
    DESCRIPTION VARCHAR(255) NOT NULL,
    RELEASE_DATE DATE        NOT NULL,
    DURATION INTEGER         NOT NULL,
    RATING_ID INTEGER        NOT NULL REFERENCES RATING
        (
         RATING_ID
            ) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS PUBLIC.DIRECTOR_FILM
(
    DIRECTOR_FILM_ID
                INTEGER
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    FILM_ID     INTEGER NOT NULL REFERENCES FILM (FILM_ID) ON DELETE CASCADE,
    DIRECTOR_ID INTEGER NOT NULL REFERENCES DIRECTOR (DIRECTOR_ID) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS DIRECTOR_FILM_FILM_ID_IDX ON PUBLIC.DIRECTOR_FILM (FILM_ID, DIRECTOR_ID);

create TABLE IF NOT EXISTS PUBLIC.GENRE_FILM
(
    GENRE_FILM_ID
             INTEGER
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
            KEY,
    FILM_ID
             INTEGER
                     NOT
                         NULL
        REFERENCES
            FILM
                (
                 FILM_ID
                    ) ON DELETE CASCADE,
    GENRE_ID INTEGER NOT NULL REFERENCES GENRE
        (
         GENRE_ID
            ) ON DELETE CASCADE
);

create unique index IF NOT EXISTS GENRE_FILM_FILM_ID_IDX ON PUBLIC.GENRE_FILM (FILM_ID, GENRE_ID);

create TABLE IF NOT EXISTS PUBLIC.USERS
(
    USERS_ID
          INTEGER
        GENERATED
            BY
            DEFAULT AS
            IDENTITY
        PRIMARY
    KEY,
    EMAIL
    VARCHAR
(
    255
) NOT NULL,
    LOGIN VARCHAR
(
    255
) NOT NULL,
    NAME VARCHAR
(
    255
) NOT NULL,
    BIRTHDAY DATE NOT NULL
    );

create TABLE IF NOT EXISTS PUBLIC.FILM_LIKES
(
    FILM_LIKES_ID
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    FILM_ID
    INTEGER
    NOT
    NULL
    REFERENCES
    FILM
(
    FILM_ID
) ON DELETE CASCADE,
    USERS_ID INTEGER NOT NULL REFERENCES USERS
(
    USERS_ID
) ON DELETE CASCADE
    );

create unique index IF NOT EXISTS FILM_LIKES_USERS_ID_IDX ON PUBLIC.FILM_LIKES (USERS_ID,FILM_ID);

create TABLE IF NOT EXISTS PUBLIC.FRIENDSHIP
(
    FRIENDSHIP_ID
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    USER_ID
    INTEGER
    NOT
    NULL
    REFERENCES
    USERS
(
    USERS_ID
    ) ON DELETE CASCADE,
    FRIEND_USER_ID INTEGER NOT NULL REFERENCES USERS
        (
         USERS_ID
            ) ON DELETE CASCADE
);
create unique index IF NOT EXISTS FRIENDSHIP_USER_ID_IDX ON PUBLIC.FRIENDSHIP (USER_ID, FRIEND_USER_ID);

create TABLE IF NOT EXISTS PUBLIC.REVIEW
(
    review_id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    content     varchar(2000) NOT NULL,
    is_positive boolean       NOT NULL,
    users_id    integer       NOT NULL REFERENCES users (users_id),
    film_id     integer       NOT NULL REFERENCES film (film_id)
);

create unique index IF NOT EXISTS REVIEW_IDX ON PUBLIC.REVIEW (film_id, users_id);

create TABLE IF NOT EXISTS PUBLIC.REVIEW_LIKE
(
    review_like_id  INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    review_id       integer NOT NULL REFERENCES REVIEW (review_id) ON delete CASCADE,
    users_id        integer NOT NULL REFERENCES users (users_id),
    like_or_dislike integer NOT NULL
);

create unique index IF NOT EXISTS REVIEW_LIKE_IDX ON PUBLIC.REVIEW_LIKE (review_id, users_id);

CREATE TABLE IF NOT EXISTS PUBLIC.USER_EVENT
(
    USER_EVENT_ID integer generated BY DEFAULT AS IDENTITY PRIMARY KEY,
    TIME_STAMP     timestamp      NOT NULL,
    USERS_ID      INTEGER      NOT NULL REFERENCES users (users_id) ON DELETE CASCADE,
    EVENT_TYPE     varchar(255) NOT NULL,
    OPERATION_TYPE     varchar(255) NOT NULL,
    ENTITY_ID      INTEGER      NOT NULL
);

alter table USER_EVENT
alter TIME_STAMP SET DEFAULT now();

delete
from REVIEW_LIKE;
alter table REVIEW_LIKE
    alter COLUMN REVIEW_LIKE_ID RESTART with 1;
delete
from REVIEW;
alter table REVIEW
    alter COLUMN REVIEW_ID RESTART with 1;
delete
from FRIENDSHIP;
alter table FRIENDSHIP
    alter COLUMN FRIENDSHIP_ID RESTART with 1;
delete
from FILM_LIKES;
alter table FILM_LIKES
    alter COLUMN FILM_LIKES_ID RESTART with 1;
delete
from GENRE_FILM;
alter table GENRE_FILM
    alter COLUMN GENRE_FILM_ID RESTART with 1;
DELETE
FROM DIRECTOR_FILM;
ALTER TABLE DIRECTOR_FILM
    ALTER COLUMN DIRECTOR_FILM_ID RESTART WITH 1;
delete
from USERS;
alter table USERS
    alter COLUMN users_id RESTART with 1;
delete
from GENRE;
alter table GENRE
    alter COLUMN genre_id RESTART with 1;
DELETE
FROM DIRECTOR;
ALTER TABLE DIRECTOR
    ALTER COLUMN director_id RESTART WITH 1;
delete
from FILM;
alter table FILM
    alter COLUMN film_id RESTART with 1;
delete
from RATING;
alter table RATING
    alter COLUMN rating_id RESTART with 1;

delete
from USER_EVENT;
alter table USER_EVENT
    alter COLUMN USER_EVENT_ID RESTART with 1;


insert into RATING (name)
values ('G'),
       ('PG'),
       ('PG-13'),
       ('R'),
       ('NC-17');

insert into GENRE (name)
values ('Комедия'),
       ('Драма'),
       ('Мультфильм'),
       ('Триллер'),
       ('Документальный'),
       ('Боевик');