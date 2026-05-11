CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS locations (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    geom GEOMETRY(Point, 4326) NOT NULL,
    description TEXT,
    image TEXT,
    address TEXT,
    rating DOUBLE PRECISION,
    icon TEXT,
    color TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS locations_geom_idx ON locations USING GIST (geom);
CREATE INDEX IF NOT EXISTS locations_name_idx ON locations (name);
CREATE INDEX IF NOT EXISTS locations_category_idx ON locations (category);
