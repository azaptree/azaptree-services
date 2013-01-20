-- Table: azaptree.t_entity

-- DROP TABLE azaptree.t_entity;

CREATE TABLE azaptree.t_entity
(
  entity_id uuid NOT NULL
)
WITH (
  OIDS=FALSE
);
ALTER TABLE azaptree.t_entity
  OWNER TO postgres;
GRANT ALL ON TABLE azaptree.t_entity TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE azaptree.t_entity TO azaptree_app;
