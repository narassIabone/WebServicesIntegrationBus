CREATE TABLE message_audit (
                               id UUID PRIMARY KEY,
                               message_id UUID NOT NULL,
                               trace_id UUID NOT NULL,
                               route_id UUID NOT NULL,
                               node_id INT NOT NULL,
                               node_type VARCHAR(50),
                               status VARCHAR(20) NOT NULL,
                               payload_before TEXT,
                               payload_after TEXT,
                               headers_snapshot JSONB,
                               context_snapshot JSONB,
                               error_message TEXT,
                               execution_time_ms BIGINT,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_message_id ON message_audit(message_id);
CREATE INDEX idx_audit_created_at ON message_audit(created_at);