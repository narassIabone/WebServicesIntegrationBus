CREATE TABLE route_ui_metadata (
                                   route_id UUID PRIMARY KEY,
                                   layout_data JSONB NOT NULL,
                                   CONSTRAINT fk_ui_metadata_route
                                       FOREIGN KEY (route_id)
                                           REFERENCES routes (route_id)
                                           ON DELETE CASCADE
);

CREATE INDEX idx_route_ui_metadata_id ON route_ui_metadata(route_id);