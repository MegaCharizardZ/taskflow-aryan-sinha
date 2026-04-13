# Taskflow

A task management REST API built with Spring Boot 4, PostgreSQL, and JWT authentication.

---

## Tech Stack

- **Runtime**: Java 21, Spring Boot 4.x
- **Persistence**: PostgreSQL, Spring Data JPA, Flyway 11
- **Security**: JWT (JJWT 0.12), Spring Security
- **Tests**: Cucumber + Testcontainers
- **Build**: Maven, Docker / Docker Compose

---

## Getting Started

**Only Docker is required.** No Java, Maven, or local PostgreSQL installation needed.

```bash
git clone <repo-url>
cd taskflow

cp .env.example .env
# open .env and set POSTGRES_USER, POSTGRES_PASSWORD, and JWT_SECRET
# POSTGRES_DB can stay as taskflow_db

docker-compose up --build
```

Docker builds the application image (Maven runs inside the build stage — no local Java needed), starts PostgreSQL, waits for it to be healthy, then starts the API. Flyway runs the schema migrations automatically on first boot. The first build takes 2–3 minutes while Maven downloads dependencies; subsequent builds are faster due to layer caching.

The API is ready when you see `Started TaskflowApplication` in the logs. Verify with:

```bash
curl http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"Password1!"}'
```

A `201 Created` response confirms everything is running. To stop:

```bash
docker-compose down      # stop containers, keep the database volume
docker-compose down -v   # stop containers and wipe the database
```

---

## Environment Variables

Copy `.env.example` to `.env` and fill in the values.

| Variable | Description | Default |
|---|---|---|
| `POSTGRES_DB` | Database name | `taskflow_db` |
| `POSTGRES_USER` | Database username | — |
| `POSTGRES_PASSWORD` | Database password | — |
| `JWT_SECRET` | HMAC-SHA256 signing key (min 32 chars) | — |

---

## API Overview

> Full request/response examples: `backend/example_response.json`

### Auth

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | No | Register a new user |
| POST | `/auth/login` | No | Login, returns JWT |

### Projects

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/projects` | Yes | List projects visible to the caller |
| POST | `/projects` | Yes | Create a project |
| GET | `/projects/{id}` | Yes | Get project with its tasks |
| PATCH | `/projects/{id}` | Yes (owner only) | Update project name/description |
| DELETE | `/projects/{id}` | Yes (owner only) | Delete project and all its tasks |

### Tasks

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/projects/{id}/tasks` | Yes | List tasks in a project |
| POST | `/projects/{id}/tasks` | Yes | Create a task |
| PATCH | `/tasks/{id}` | Yes (creator or assignee) | Update task fields |
| DELETE | `/tasks/{id}` | Yes (project owner or creator) | Delete a task |
| GET | `/projects/{id}/stats` | Yes | Task counts grouped by assignee and status |

### Access rules

- A project is **visible** to a user if they own it, are assigned to a task in it, or created a task in it.
- **PATCH** on a task is restricted to the task creator or the assignee.
- **DELETE** on a task is restricted to the project owner or the task creator.

---

## Database Migrations

Flyway manages the schema. Migration files live in `backend/src/main/resources/db/migration/`.

| File | What it does |
|---|---|
| `V1__Initial_Schema.sql` | Creates `users`, `projects`, `tasks` tables and indexes |
| `V2__Add_task_created_by.sql` | Adds `created_by` column and index to `tasks` |
| `V3__Seed_data.sql` | Seeds a demo user, project, and tasks for local dev |
| `U1`, `U2`, `U3` | Undo migrations (reverse of each V migration above) |

> Undo migrations require Flyway Teams/Enterprise. In Community Edition they serve as a rollback runbook.

---

## Running Tests

Tests require Java 21 and Maven (they spin up their own Testcontainers PostgreSQL instance and cannot run purely inside Docker Compose).

```bash
cd backend
mvn test -P cucumber
```
Note: You need to have mvn installed in your local machine for running the cucumber test suite

---

## Honest Reflection

### Design Decisions

**Added created by column in tasks table** 
This allows us to track who created each task. I have tried to make it similar to Jira, where we have EPIC owners, 
equivalent to project owners here. Then we have task creators and assignees. Task is equivalent to a story created under a 
Jira Epic.



### Shortcuts taken

**No project membership model.**
Access is inferred — a user gains visibility into a project by creating or being assigned a task. There is no explicit invite or member list. This means there is no way to add a collaborator to a project without also giving them a task, and no way to remove access without removing all their tasks.

**No pagination.**
`GET /projects` and `GET /projects/{id}/tasks` return all rows. This works fine for small datasets, 
however we should have a limit and offset for paginating through results.

**Task PATCH does not support clearing fields.**
If an assignee is set, there is no way to unassign the task — passing `"assignee": null` is silently ignored. 

**JWT with no refresh token.**
Generally, JWT access tokens should be short-lived, with a long lived refresh token. 
This can be done more accurately

**No rate limiting on any endpoints.**
There is no server side rate limiter in place for any of the API endpoints. This can lead to a DDoS attack

**Test coverage is auth-only.**
The Cucumber suite covers authentication and authorization scenarios well. Tests haven't been created for project and task CRUD flows and their edge cases.

**Error responses use string messages, not codes.**
Error bodies like `{"error": "Invalid credentials"}` are readable but not machine-friendly. A structured error code (e.g. `AUTH_INVALID_CREDENTIALS`) would let clients handle errors programmatically without string matching.

### What I would add or improve

- **Project membership**: As of right now, any authenticated user can create a project and add tasks to it, in the future
roles such as Admin, TaskCreator etc should be added, so that only certain users can have access to create projects and
tasks.
- **Pagination**: List endpoints should support cursor-based pagination with `limit` and `offset` parameters
- **Refresh tokens**: short-lived access token + long-lived refresh token stored securely
- **Rate limiting**: Server side rate limiting, maybe using Bucket4j, to prevent abuse and DDoS attacks
- **Soft deletes**: `deleted_at` timestamp instead of hard deletes, so data is recoverable
- **OpenAPI docs**: Swagger docs for the api specs and example requests/responses
- **Structured error codes**: proper error codes in all error responses
- **Expanded test suite**: Comprehensive Cucumber scenarios for project/task CRUD and the access-control matrix
- **Tracing and Metrics**: Add counters and timers for key operations, and distributed tracing with something 
like OpenTelemetry for better observability in production.
- **Add tokens per user**: Add tokens per user, so that a particular user can only have a set number of tokens, and
create a limited number of projects and tasks. This can also help with rate limiting. We can also have tiers with
increased tokens for premium users.
- **List all tasks by priority and status**: For the endpoint to list the tasks, the tasks should be ordered by priority
and status, so that the important and pending tasks are shown first
- **More analysis of bcrypt cost factor**: I chose the default cost factor of 12, the choice of cost factor could have
been done with more analysis. This is the minimum accepted security cost factor.
- **Improper error handling**: In some places, where we should throw a different error such as internal server error,
application is throwing client side error
- **Unit tests**: Self explainatory