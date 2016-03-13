DO $$
BEGIN

CREATE TABLE IF NOT EXISTS packets
(
  destination character varying(255),
  filename character varying(255),
  info character varying(16384),
  length integer,
  "number" integer,
  protocol character varying(255),
  source character varying(255),
  "timestamp" timestamp without time zone,
  id integer NOT NULL,
  CONSTRAINT packets_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

IF NOT EXISTS (
    SELECT 1
    FROM   pg_class c
    JOIN   pg_namespace n ON n.oid = c.relnamespace
    WHERE  c.relname = 'timestamp'
    AND    n.nspname = 'public' -- 'public' by default
    ) THEN

    CREATE INDEX "timestamp" ON packets USING btree ("timestamp");
END IF;

END$$;