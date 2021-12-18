CREATE TABLE friends_raw
(
    user   varchar(36) NOT NULL,
    friend varchar(36) NOT NULL,
    PRIMARY KEY (user, friend),
    CONSTRAINT smaller_user_first CHECK ( friend < user )
);

CREATE TABLE friend_requests
(
    user    varchar(36)    NOT NULL,
    friend  varchar(36)    NOT NULL,
    ignored bool DEFAULT 0 NOT NULL,
    PRIMARY KEY (user, friend)
);

CREATE TABLE blocked
(
    source varchar(36) NOT NULL,
    target varchar(36) NOT NULL,
    PRIMARY KEY (source, target)
);

CREATE VIEW friends AS
(
    SELECT user, friend
    FROM friends_raw

    UNION ALL

    SELECT friend as user, user as friend
    FROM friends_raw
);