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