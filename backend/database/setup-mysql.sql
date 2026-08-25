-- Replace __DB_PASSWORD__ with a local development password before running this file.

CREATE DATABASE IF NOT EXISTS fracturecare
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'fracturecare'@'localhost'
    IDENTIFIED BY '__DB_PASSWORD__';

ALTER USER 'fracturecare'@'localhost'
    IDENTIFIED BY '__DB_PASSWORD__';

GRANT ALL PRIVILEGES ON fracturecare.*
    TO 'fracturecare'@'localhost';

FLUSH PRIVILEGES;
