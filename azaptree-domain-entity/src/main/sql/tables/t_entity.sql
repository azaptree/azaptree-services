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
