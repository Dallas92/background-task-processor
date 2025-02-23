CREATE TABLE tasks (
    id          BIGSERIAL PRIMARY KEY,
    min         INTEGER NOT NULL,
    max         INTEGER NOT NULL,
    count       INTEGER NOT NULL,
    counter     INTEGER NOT NULL,
    is_complete BOOLEAN NOT NULL,
    version     INTEGER NOT NULL
);
