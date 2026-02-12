-- Create events table
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    from_value VARCHAR(255) NOT NULL,
    to_value VARCHAR(255) NOT NULL
);

-- Create index for event type queries
CREATE INDEX IF NOT EXISTS idx_events_type ON events(type);

-- Add check constraint for event type
ALTER TABLE events ADD CONSTRAINT chk_event_type CHECK (type IN ('NAVIGATION', 'ACTION'));

-- Add comments
COMMENT ON TABLE events IS 'User events tracking table';
COMMENT ON COLUMN events.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN events.type IS 'Event type: NAVIGATION or ACTION';
COMMENT ON COLUMN events.from_value IS 'Source location or state';
COMMENT ON COLUMN events.to_value IS 'Destination location or state';
