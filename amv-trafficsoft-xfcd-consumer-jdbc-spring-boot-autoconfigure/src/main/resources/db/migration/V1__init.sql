CREATE TABLE openmrc_request (
  id   BIGSERIAL,
  type VARCHAR(32)    NOT NULL,
  json VARCHAR(10000) NOT NULL,
  PRIMARY KEY (id)
);
