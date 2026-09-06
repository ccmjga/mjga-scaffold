CREATE SCHEMA IF NOT EXISTS mjga_platform;
SET search_path TO mjga_platform;

CREATE TABLE IF NOT EXISTS event_publication
(
    id                     UUID NOT NULL PRIMARY KEY,
    listener_id            TEXT NOT NULL,
    event_type             TEXT NOT NULL,
    serialized_event       TEXT NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    status                 TEXT,
    completion_attempts    INT,
    last_resubmission_date TIMESTAMP WITH TIME ZONE
);
-- [jooq ignore start]
CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx
    ON event_publication USING hash(serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx
    ON event_publication (completion_date);
-- [jooq ignore stop]

CREATE TABLE IF NOT EXISTS event_publication_audit
(
    id             UUID NOT NULL PRIMARY KEY,
    publication_id UUID,
    listener_id    TEXT,
    event_type     TEXT,
    action         TEXT NOT NULL,
    operator_id    TEXT NOT NULL,
    reason         TEXT NOT NULL,
    before_status  TEXT,
    after_status   TEXT,
    correlation_id TEXT NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL
);
