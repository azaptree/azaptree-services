-- Table: azaptree.t_entity_audit_action

-- DROP TABLE azaptree.t_entity_audit_action;

CREATE TABLE azaptree.t_entity_audit_action
(
  audit_action_id smallint NOT NULL,
  autit_action_name text NOT NULL,
  CONSTRAINT pk_entity_audit_action PRIMARY KEY (audit_action_id),
  CONSTRAINT uk_entity_audit_action UNIQUE (autit_action_name)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE azaptree.t_entity_audit_action
  OWNER TO postgres;
GRANT ALL ON TABLE azaptree.t_entity_audit_action TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE azaptree.t_entity_audit_action TO azaptree_app;


insert into azaptree.t_entity_audit_action values (0,'CREATED');
insert into azaptree.t_entity_audit_action values (1,'UPDATED');
insert into azaptree.t_entity_audit_action values (2,'DELETED');