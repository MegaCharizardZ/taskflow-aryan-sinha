-- U4__Refine_task_indexes.sql (undo V4)
DROP INDEX IF EXISTS idx_tasks_project_assignee_creator;

CREATE INDEX idx_tasks_status ON tasks(status);
