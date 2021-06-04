CREATE DATABASE example1 OWNER user1;

\connect example1 ;

CREATE TYPE AGGREGATES_ENUM AS ENUM ('User');

-- commands

-- https://github.com/thenativeweb/commands-events/issues/1#issuecomment-385862281

CREATE TABLE commands (
      id UUID NOT NULL PRIMARY KEY,
      causation_id UUID NOT NULL,
      correlation_id UUID NOT NULL,
      cmd_payload JSONB NOT NULL,
      inserted_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    )
 ;

-- indexes

-- TODO CREATE INDEX idx_causation_id ON commands (causation_id);

-- events

CREATE TABLE events (
      sequence BIGSERIAL NOT NULL,
      id UUID NOT NULL DEFAULT gen_random_uuid(),
      event_payload JSONB NOT NULL,
      ar_name AGGREGATES_ENUM NOT NULL,
      ar_id UUID NOT NULL,
      version INTEGER NOT NULL,
      causation_id UUID NOT NULL,
      correlation_id UUID NOT NULL,
      inserted_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (ar_id, sequence)
    );

-- indexes

-- CREATE INDEX idx_causation_id ON events (causation_id);

CREATE INDEX idx_ar ON events (ar_name, ar_id);

-- projections

CREATE TABLE projections (
   name VARCHAR(36) PRIMARY KEY NOT NULL,
   last_offset BIGINT
);

INSERT INTO projections (name, last_offset) values ('nats', 0);
INSERT INTO projections (name, last_offset) values ('users', 0);

--  snapshots tables

CREATE TABLE user_snapshots (
      ar_id UUID NOT NULL,
      version INTEGER,
      json_content JSONB NOT NULL,
      PRIMARY KEY (ar_id)
    );

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

--# DB Version: 13
--# OS Type: linux
--# DB Type: oltp
--# Total Memory (RAM): 8 GB
--# CPUs num: 8
--# Connections num: 32
--# Data Storage: ssd

ALTER SYSTEM SET
 max_connections = '32';
ALTER SYSTEM SET
 shared_buffers = '2GB';
ALTER SYSTEM SET
 effective_cache_size = '6GB';
ALTER SYSTEM SET
 maintenance_work_mem = '512MB';
ALTER SYSTEM SET
 checkpoint_completion_target = '0.9';
ALTER SYSTEM SET
 wal_buffers = '16MB';
ALTER SYSTEM SET
 default_statistics_target = '100';
ALTER SYSTEM SET
 random_page_cost = '1.1';
ALTER SYSTEM SET
 effective_io_concurrency = '200';
ALTER SYSTEM SET
 work_mem = '16MB';
ALTER SYSTEM SET
 min_wal_size = '2GB';
ALTER SYSTEM SET
 max_wal_size = '8GB';
ALTER SYSTEM SET
 max_worker_processes = '8';
ALTER SYSTEM SET
 max_parallel_workers_per_gather = '4';
ALTER SYSTEM SET
 max_parallel_workers = '8';
ALTER SYSTEM SET
 max_parallel_maintenance_workers = '4';