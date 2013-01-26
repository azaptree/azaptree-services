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
CREATE TABLE azaptree.t_hashed_credential
(
   name text NOT NULL, 
   subject_id uuid NOT NULL, 
   hash bytea, 
   hash_algorithm text NOT NULL, 
   hash_iterations integer NOT NULL DEFAULT 1024, 
   salt bytea NOT NULL, 
   CONSTRAINT pk_hashed_credential PRIMARY KEY (entity_id), 
   CONSTRAINT uk_hashed_credential UNIQUE (subject_id, name), 
   CONSTRAINT fk_hashed_credential_subject FOREIGN KEY (subject_id) REFERENCES azaptree.t_subject (entity_id) ON UPDATE NO ACTION ON DELETE CASCADE
) 
INHERITS (azaptree.t_versioned_entity)
WITH (
  OIDS = FALSE
)
;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE azaptree.t_hashed_credential TO GROUP azaptree_app;