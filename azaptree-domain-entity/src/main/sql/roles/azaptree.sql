-- Role: azaptree

-- DROP ROLE azaptree;

CREATE ROLE azaptree LOGIN
  ENCRYPTED PASSWORD 'md560b1f4e557aca8b32b7bc9ca87ca641e'
  NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;
GRANT azaptree_app TO azaptree;
