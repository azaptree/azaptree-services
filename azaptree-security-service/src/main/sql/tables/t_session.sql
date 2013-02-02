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
-- Table: azaptree.t_session

-- DROP TABLE azaptree.t_session;

CREATE TABLE azaptree.t_session
(
-- Inherited from table azaptree.t_entity:  entity_id uuid NOT NULL,
  subject_id uuid NOT NULL,
  created_on timestamp with time zone NOT NULL DEFAULT now(),
  last_accessed_on timestamp with time zone NOT NULL,
  timeout integer NOT NULL DEFAULT 1800, -- The time in seconds that the session session may remain idle before expiring.
  host inet,
  CONSTRAINT pk_sessions PRIMARY KEY (entity_id),
  CONSTRAINT fk_session_subject FOREIGN KEY (subject_id)
      REFERENCES azaptree.t_subject (entity_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
INHERITS (azaptree.t_entity)
WITH (
  OIDS=FALSE
);
ALTER TABLE azaptree.t_session
  OWNER TO postgres;
GRANT ALL ON TABLE azaptree.t_session TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE azaptree.t_session TO azaptree_app;
COMMENT ON COLUMN azaptree.t_session.timeout IS 'The time in seconds that the session session may remain idle before expiring.';


-- Index: azaptree.fki_session_subject

-- DROP INDEX azaptree.fki_session_subject;

CREATE INDEX fki_session_subject
  ON azaptree.t_session
  USING btree
  (subject_id);

