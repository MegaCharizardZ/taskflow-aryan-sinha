-- U1__Initial_Schema.sql  (undo for V1)
-- Drop in reverse dependency order: tasks → projects → users
-- Indexes are dropped automatically by PostgreSQL when their table is dropped.

DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;
