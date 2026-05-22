CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

ALTER TABLE messages ALTER COLUMN id TYPE UUID USING (id::uuid);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='messages' AND column_name='route_id') THEN
ALTER TABLE messages ADD COLUMN route_id UUID;
END IF;
END $$;

CREATE TABLE IF NOT EXISTS routes (
                                      route_id UUID PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS nodes (
                                     node_id UUID PRIMARY KEY,
                                     business_id INTEGER,
                                     route_id UUID,
                                     type VARCHAR(50),
    is_start BOOLEAN DEFAULT FALSE,
    input_topic VARCHAR(255),
    CONSTRAINT fk_route_node FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS node_config_params (
                                                  node_id UUID NOT NULL,
                                                  param_key VARCHAR(255) NOT NULL,
    param_value VARCHAR(1000),
    PRIMARY KEY (node_id, param_key),
    CONSTRAINT fk_node_params FOREIGN KEY (node_id) REFERENCES nodes(node_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS links (
                                     link_id UUID PRIMARY KEY,
                                     route_id UUID,
                                     from_node_id INTEGER,
                                     to_node_id INTEGER,
                                     output_topic VARCHAR(255),
    async BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_route_link FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS link_field_mappings (
                                                   link_id UUID NOT NULL,
                                                   source_field VARCHAR(255) NOT NULL,
    target_field VARCHAR(255) NOT NULL,
    PRIMARY KEY (link_id, source_field),
    CONSTRAINT fk_link_mapping FOREIGN KEY (link_id) REFERENCES links(link_id) ON DELETE CASCADE
    );