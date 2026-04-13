-- V3__Refine_task_indexes.sql
-- Drop the low-value single-column status index (no query filters by status alone).
-- Replace with composite indexes that match actual query patterns.

-- Covers isAccessibleByUser EXISTS checks and countByAssigneeAndStatusForProject GROUP BY
CREATE INDEX idx_tasks_project_assignee_creator ON tasks(project_id, assignee_id, created_by);
