CREATE TABLE messages (
                          id        VARCHAR(255) PRIMARY KEY, -- Временно оставляем VARCHAR, его изменит V4
                          payload   TEXT NOT NULL,
                          status    VARCHAR(255),
                          timestamp TIMESTAMP(6)
);