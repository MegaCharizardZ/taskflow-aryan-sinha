-- U2__Add_task_created_by.sql  (undo for V2)
-- Remove the created_by column and its index.

DROP INDEX IF EXISTS idx_tasks_created_by;
ALTER TABLE tasks DROP COLUMN IF EXISTS created_by;
