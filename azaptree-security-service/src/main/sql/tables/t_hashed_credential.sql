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
-- Table: azaptree.t_hashed_credential

-- DROP TABLE azaptree.t_hashed_credential;

CREATE TABLE azaptree.t_hashed_credential
(
-- Inherited from table azaptree.t_versioned_entity:  entity_id uuid NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_version bigint NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_created_on timestamp with time zone NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_created_by uuid,
-- Inherited from table azaptree.t_versioned_entity:  entity_updated_on timestamp with time zone NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_updated_by uuid,
  name text NOT NULL,
  subject_id uuid NOT NULL,
  hash bytea NOT NULL,
  hash_algorithm text NOT NULL,
  hash_iterations integer NOT NULL DEFAULT 1024,
  salt bytea NOT NULL,
  hash_service_config_id uuid NOT NULL,
  expires_on timestamp with time zone,
  CONSTRAINT pk_hashed_credential PRIMARY KEY (entity_id),
  CONSTRAINT fk_hashed_credential_hash_service_config FOREIGN KEY (hash_service_config_id)
      REFERENCES azaptree.t_hash_service_config (entity_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_hashed_credential_subject FOREIGN KEY (subject_id)
      REFERENCES azaptree.t_subject (entity_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT uk_hashed_credential UNIQUE (subject_id, name)
)
INHERITS (azaptree.t_versioned_entity)
WITH (
  OIDS=FALSE
);
ALTER TABLE azaptree.t_hashed_credential
  OWNER TO postgres;
GRANT ALL ON TABLE azaptree.t_hashed_credential TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE azaptree.t_hashed_credential TO azaptree_app;

-- Index: azaptree.fki_hashed_credential_hash_service_config

-- DROP INDEX azaptree.fki_hashed_credential_hash_service_config;

CREATE INDEX fki_hashed_credential_hash_service_config
  ON azaptree.t_hashed_credential
  USING btree
  (hash_service_config_id);

-- Index: azaptree.fki_hashed_credential_subject

-- DROP INDEX azaptree.fki_hashed_credential_subject;

CREATE INDEX fki_hashed_credential_subject
  ON azaptree.t_hashed_credential
  USING btree
  (subject_id);