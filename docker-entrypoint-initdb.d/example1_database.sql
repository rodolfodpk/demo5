CREATE DATABASE example1 OWNER user1;

\connect example1 ;

CREATE TABLE projections (
   name VARCHAR(36) PRIMARY KEY NOT NULL,
   last_offset BIGINT
);

INSERT INTO projections (name, last_offset) values ('nats', 0);
INSERT INTO projections (name, last_offset) values ('users', 0);

-- read model

CREATE TABLE users_view (
    id UUID NOT NULL,
    name VARCHAR NOT NULL,
    email VARCHAR NOT NULL,
    password VARCHAR NOT NULL,
    is_active BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_email ON users_view (email);

--  snapshots table

CREATE TABLE snapshots (
      ar_id UUID NOT NULL,
      ar_type text NOT NULL,
      version INTEGER,
      json_content JSONB NOT NULL,
      PRIMARY KEY (ar_id, ar_type)
    )
--    PARTITION BY hash(ar_id, ar_type)
;

-- 3 partitions

--CREATE TABLE snapshots_0 PARTITION OF snapshots
--    FOR VALUES WITH (MODULUS 3, REMAINDER 0);
--CREATE TABLE snapshots_1 PARTITION OF snapshots
--    FOR VALUES WITH (MODULUS 3, REMAINDER 1);
--CREATE TABLE snapshots_2 PARTITION OF snapshots
--    FOR VALUES WITH (MODULUS 3, REMAINDER 2);

-- commands

-- https://github.com/thenativeweb/commands-events/issues/1#issuecomment-385862281

CREATE TABLE commands (
      cmd_id UUID NOT NULL PRIMARY KEY,
      cmd_payload JSONB NOT NULL,
      inserted_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    )
 ;

-- indexes

CREATE UNIQUE INDEX cmd_id_idx ON commands (cmd_id);

-- events

CREATE TABLE events (
      sequence BIGSERIAL NOT NULL,
      event_payload JSONB NOT NULL,
      ar_name text NOT NULL,
      ar_id UUID NOT NULL,
      version INTEGER NOT NULL,
      id UUID NOT NULL DEFAULT gen_random_uuid(),
      causation_id UUID NOT NULL,
      correlation_id UUID NOT NULL,
      inserted_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      UNIQUE (ar_id, version) -- to help lookup by entity id ordered by version
    )
--      PARTITION BY hash(ar_id) -- all related events within same partition
    ;

CREATE INDEX sequence_idx ON events using brin (sequence);

-- 3 partitions

--CREATE TABLE events_0 PARTITION OF events
--    FOR VALUES WITH (MODULUS 3, REMAINDER 0);
--CREATE TABLE events_1 PARTITION OF events
--    FOR VALUES WITH (MODULUS 3, REMAINDER 1);
--CREATE TABLE events_2 PARTITION OF events
--    FOR VALUES WITH (MODULUS 3, REMAINDER 2);

-- An implementation avoiding gaps in event sequence: https://dev.to/kspeakman/event-storage-in-postgres-4dk2
