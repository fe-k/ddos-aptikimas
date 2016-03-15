DO $$
BEGIN

CREATE TABLE IF NOT EXISTS packets
(
	id integer NOT NULL,
	"timestamp" timestamp without time zone,
	source character varying(255),
  	destination character varying(255),
  	protocol character varying(255),
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