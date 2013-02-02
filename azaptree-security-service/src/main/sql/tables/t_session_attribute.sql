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
-- Table: azaptree.t_session_attribute

-- DROP TABLE azaptree.t_session_attribute;

CREATE TABLE azaptree.t_session_attribute
(
-- Inherited from table azaptree.t_entity:  entity_id uuid NOT NULL,
  name text NOT NULL,
  session_id uuid NOT NULL,
  value json NOT NULL,
  CONSTRAINT pk_session_attribute PRIMARY KEY (entity_id),
  CONSTRAINT fk_session_attribute_session FOREIGN KEY (session_id)
      REFERENCES azaptree.t_session (entity_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT uk_session_attribute UNIQUE (session_id, name)
)
INHERITS (azaptree.t_entity)
WITH (
  OIDS=FALSE
);
ALTER TABLE azaptree.t_session_attribute
  OWNER TO postgres;
GRANT ALL ON TABLE azaptree.t_session_attribute TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE azaptree.t_session_attribute TO azaptree_app;


