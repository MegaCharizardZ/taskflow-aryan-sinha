-- V4__Seed_data.sql
-- Seed data for development / testing.
-- Password for test@example.com is: password123  (bcrypt cost 12)

INSERT INTO users (id, name, email, password)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'test_user',
    'test@example.com',
    '$2y$12$m4nXhoXrY2c0A50PHaSk7uk6NDy7LXIFvVWr1euvC5Hu.ZPXadV6G'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO projects (id, name, description, owner_id)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'Taskflow Demo',
    'Sample project seeded for development and testing.',
    'a0000000-0000-0000-0000-000000000001'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO tasks (id, title, description, task_status, task_priority, project_id, assignee_id, created_by)
VALUES
    (
        'c0000000-0000-0000-0000-000000000001',
        'Set up CI/CD pipeline',
        'Configure GitHub Actions for automated testing and deployment.',
        'DONE',
        'HIGH',
        'b0000000-0000-0000-0000-000000000001',
        'a0000000-0000-0000-0000-000000000001',
        'a0000000-0000-0000-0000-000000000001'
    ),
    (
        'c0000000-0000-0000-0000-000000000002',
        'Implement authentication',
        'Add JWT-based login and registration endpoints.',
        'IN_PROGRESS',
        'HIGH',
        'b0000000-0000-0000-0000-000000000001',
        'a0000000-0000-0000-0000-000000000001',
        'a0000000-0000-0000-0000-000000000001'
    ),
    (
        'c0000000-0000-0000-0000-000000000003',
        'Write API documentation',
        'Document all endpoints with request/response examples.',
        'TODO',
        'MEDIUM',
        'b0000000-0000-0000-0000-000000000001',
        NULL,
        'a0000000-0000-0000-0000-000000000001'
    )
ON CONFLICT (id) DO NOTHING;
