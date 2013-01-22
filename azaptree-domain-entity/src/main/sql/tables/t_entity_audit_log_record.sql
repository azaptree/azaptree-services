---
-- #%L
-- AZAPTREE-DOMAIN-ENTITY
-- %%
-- Copyright (C) 2012 - 2013 AZAPTREE.COM
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
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

