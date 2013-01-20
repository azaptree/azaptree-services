-- Table: azaptree.t_versioned_entity

-- DROP TABLE azaptree.t_versioned_entity;

CREATE TABLE azaptree.t_versioned_entity
(
-- Inherited from table azaptree.t_entity:  entity_id uuid NOT NULL,
  entity_version bigint NOT NULL,
  entity_created_on timestamp with time zone NOT NULL,
  entity_created_by uuid,
  entity_updated_on timestamp with time zone NOT NULL,
  entity_updated_by uuid
)
INHERITS (azaptree.t_entity)
WITH (
  OIDS=FALSE
);
ALTER TABLE azaptree.t_versioned_entity
  OWNER TO postgres;
GRANT ALL ON TABLE azaptree.t_versioned_entity TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE azaptree.t_versioned_entity TO azaptree_app;
