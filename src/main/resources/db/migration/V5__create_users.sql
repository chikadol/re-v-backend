-- V5__create_users.sql
CREATE SCHEMA IF NOT EXISTS rev;

CREATE TABLE IF NOT EXISTS rev.users (
                                         id UUID PRIMARY KEY,
                                         username VARCHAR(100) NOT NULL,
                                         password VARCHAR(255) NOT NULL,
                                         email VARCHAR(255) NOT NULL
);
