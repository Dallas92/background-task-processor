CREATE TABLE tasks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    min         INT NOT NULL,
    max         INT NOT NULL,
    count       INT NOT NULL,
    counter     INT NULL,
    is_complete BOOLEAN NULL,
    version     INT NOT NULL
);
