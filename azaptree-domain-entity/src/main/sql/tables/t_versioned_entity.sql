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
