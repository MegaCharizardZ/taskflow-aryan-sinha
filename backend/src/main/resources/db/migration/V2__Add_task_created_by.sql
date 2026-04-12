-- V2__Add_task_created_by.sql
-- Track which user created each task (required for "project owner OR task creator can delete" rule)
ALTER TABLE tasks ADD COLUMN created_by UUID REFERENCES users(id);

CREATE INDEX idx_tasks_created_by ON tasks(created_by);
