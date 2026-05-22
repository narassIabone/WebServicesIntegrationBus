CREATE TABLE aggregation_storage (
                                     correlation_id UUID PRIMARY KEY,
                                     expected_count INT NOT NULL,
                                     received_count INT NOT NULL DEFAULT 0,
                                     payloads JSONB NOT NULL DEFAULT '[]'::jsonb,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_aggregation_updated_at ON aggregation_storage(updated_at);