---
-- #%L
-- AZAPTREE SECURITY SERVICE
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
-- Table: azaptree.t_subject

-- DROP TABLE azaptree.t_subject;

CREATE TABLE azaptree.t_subject
(
-- Inherited from table azaptree.t_versioned_entity:  entity_id uuid NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_version bigint NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_created_on timestamp with time zone NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_created_by uuid,
-- Inherited from table azaptree.t_versioned_entity:  entity_updated_on timestamp with time zone NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_updated_by uuid,
  max_sessions integer NOT NULL DEFAULT 1,
  status integer NOT NULL,
  status_timestamp timestamp with time zone NOT NULL,
  consec_auth_failed_count smallint NOT NULL DEFAULT 0,
  last_auth_failed_ts timestamp with time zone,
  CONSTRAINT pk_subject PRIMARY KEY (entity_id)
)
INHERITS (azaptree.t_versioned_entity)
WITH (
  OIDS=FALSE
);
ALTER TABLE azaptree.t_subject
  OWNER TO postgres;
GRANT ALL ON TABLE azaptree.t_subject TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE azaptree.t_subject TO azaptree_app;

-- Index: azaptree.idx_subject_id_version

-- DROP INDEX azaptree.idx_subject_id_version;

CREATE INDEX idx_subject_id_version
  ON azaptree.t_subject
  USING btree
  (entity_id, entity_version);


