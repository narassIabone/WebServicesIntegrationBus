-- Безопасное добавление route_name с проверкой
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='message_audit' AND column_name='route_name') THEN
ALTER TABLE message_audit ADD COLUMN route_name VARCHAR(255);
END IF;
END $$;

ALTER TABLE message_audit ADD COLUMN node_config JSONB;

CREATE INDEX IF NOT EXISTS idx_audit_route_name ON message_audit(route_name);