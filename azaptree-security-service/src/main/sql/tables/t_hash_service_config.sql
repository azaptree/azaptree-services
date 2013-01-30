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
-- Table: azaptree.t_hash_service_config

-- DROP TABLE azaptree.t_hash_service_config;

CREATE TABLE azaptree.t_hash_service_config
(
-- Inherited from table azaptree.t_versioned_entity:  entity_id uuid NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_version bigint NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_created_on timestamp with time zone NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_created_by uuid,
-- Inherited from table azaptree.t_versioned_entity:  entity_updated_on timestamp with time zone NOT NULL,
-- Inherited from table azaptree.t_versioned_entity:  entity_updated_by uuid,
  name text NOT NULL,
  private_salt bytea NOT NULL,
  hash_iterations integer NOT NULL,
  hash_algorithm text NOT NULL,
  secure_rand_next_bytes_size integer NOT NULL,
  CONSTRAINT pk_hash_service_config PRIMARY KEY (entity_id),
  CONSTRAINT uk_hash_service_config UNIQUE (name)
)
INHERITS (azaptree.t_versioned_entity)
WITH (
  OIDS=FALSE
);
ALTER TABLE azaptree.t_hash_service_config
  OWNER TO postgres;
GRANT ALL ON TABLE azaptree.t_hash_service_config TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE azaptree.t_hash_service_config TO azaptree_app;
