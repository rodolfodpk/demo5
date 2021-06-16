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
      version INTEGER,
      json_content JSONB NOT NULL,
      PRIMARY KEY (ar_id)
    )
--    PARTITION BY hash(ar_id)
;

-- 3 partitions

--CREATE TABLE snapshots_0 PARTITION OF snapshots
--    FOR VALUES WITH (MODULUS 3, REMAINDER 0);
--CREATE TABLE snapshots_1 PARTITION OF snapshots
--    FOR VALUES WITH (MODULUS 3, REMAINDER 1);
--CREATE TABLE snapshots_2 PARTITION OF snapshots
--    FOR VALUES WITH (MODULUS 3, REMAINDER 2);

-- correlations table

CREATE TYPE CORRELATION_TYPE_ENUM AS ENUM ('Command', 'Event');

CREATE TABLE correlations (
      id BIGSERIAL PRIMARY KEY,
      msg_id BIGINT NOT NULL,
      msg_type CORRELATION_TYPE_ENUM NOT NULL,
      causation_id BIGINT NOT NULL,
      causation_type CORRELATION_TYPE_ENUM NOT NULL,
      correlation_id BIGINT NOT NULL,
      correlation_type CORRELATION_TYPE_ENUM NOT NULL
);

-- commands

-- https://github.com/thenativeweb/commands-events/issues/1#issuecomment-385862281

CREATE TABLE commands (
      id BIGSERIAL NOT NULL PRIMARY KEY,
      cmd_id UUID NOT NULL,
      cmd_payload JSONB NOT NULL,
--      correlation_id BIGINT,
      inserted_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    )
 ;

-- indexes

CREATE UNIQUE INDEX cmd_id_idx ON commands (cmd_id);

-- events

-- it must have version so it accepts event patching

CREATE TABLE events (
      sequence BIGSERIAL NOT NULL,
      event_payload JSONB NOT NULL,
      ar_name text NOT NULL,
      ar_id UUID NOT NULL,
      version INTEGER NOT NULL,
      cmd_id BIGINT,
--      correlation_id BIGINT,
      inserted_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      UNIQUE (ar_id, version) -- to help lookup by entity id ordered by version
    )
--      PARTITION BY hash(ar_id) -- all related events within same partition
    ;

--CREATE INDEX sequence_idx ON events using brin (sequence);

-- 3 partitions

--CREATE TABLE events_0 PARTITION OF events
--    FOR VALUES WITH (MODULUS 3, REMAINDER 0);
--CREATE TABLE events_1 PARTITION OF events
--    FOR VALUES WITH (MODULUS 3, REMAINDER 1);
--CREATE TABLE events_2 PARTITION OF events
--    FOR VALUES WITH (MODULUS 3, REMAINDER 2);
