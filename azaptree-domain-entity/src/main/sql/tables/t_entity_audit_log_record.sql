-- Table: azaptree.t_entity_audit_log_record

-- DROP TABLE azaptree.t_entity_audit_log_record;

CREATE TABLE azaptree.t_entity_audit_log_record
(
-- Inherited from table azaptree.t_entity:  entity_id uuid NOT NULL,
  audited_entity_id uuid NOT NULL,
  audit_action_id smallint NOT NULL,
  created_on timestamp with time zone NOT NULL DEFAULT now(), -- When the audit log record was created
  entity_type text NOT NULL, -- entity class name
  entity_json json NOT NULL,
  CONSTRAINT pk_entity_audit_log_record PRIMARY KEY (entity_id),
  CONSTRAINT fk_audit_action FOREIGN KEY (audit_action_id)
      REFERENCES azaptree.t_entity_audit_action (audit_action_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
INHERITS (azaptree.t_entity)
WITH (
  OIDS=FALSE
);
ALTER TABLE azaptree.t_entity_audit_log_record
  OWNER TO postgres;
COMMENT ON COLUMN azaptree.t_entity_audit_log_record.created_on IS 'When the audit log record was created';
COMMENT ON COLUMN azaptree.t_entity_audit_log_record.entity_type IS 'entity class name';

