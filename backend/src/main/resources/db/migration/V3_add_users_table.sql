CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL
);
INSERT INTO users (username, password, role)
VALUES ('admin', '$2a$10$cA3nklkzUeV/4Cw4NEQMtOlsk5O/8hG7OYMVXpIaSxYwRbgcbhS5a', 'ROLE_ADMIN');