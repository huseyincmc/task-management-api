# Task Management API

Educational Spring Boot backend project for practicing modern Java backend development, Docker, Docker Compose, PostgreSQL, Redis, and microservice communication.

The goal of this project is not only to build a task API, but also to learn how a real backend application is structured, containerized, and prepared for future technologies such as Redis, Kafka, JWT, and automated tests.

## Tech Stack

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Security
- PostgreSQL 16
- Redis 7.4
- Docker
- Docker Compose
- Maven

## Current Features

- Layered architecture
- Task CRUD endpoints
- Request/response DTOs
- Validation support
- Global exception handling
- PostgreSQL integration
- Redis cache integration
- Dockerized Spring Boot services
- Docker Compose setup for task-service + notification-service + PostgreSQL + Redis
- Persistent PostgreSQL data with Docker volume
- Basic synchronous REST communication between services

## Services

| Service | Local Port | Docker Port | Responsibility |
| --- | --- | --- | --- |
| `task-service` | `8081` | `8082` | Manages tasks, PostgreSQL data, Redis cache/counters/events |
| `notification-service` | `8083` | `8083` | Receives task notification requests and logs them |

Current service-to-service flow:

- `task-service` creates a task
- `task-service` sends a REST request to `notification-service`
- `notification-service` receives the request and logs the notification

## Redis Cache Behavior

Redis is used as a cache for read-heavy task endpoints:

- `GET /api/tasks` caches the full task list with key `tasks::all`
- `GET /api/tasks/{id}` caches a single task with key `tasks::{id}`
- `POST`, `PUT`, and `DELETE` clear the task cache so stale data is not returned
- Cached entries currently expire after 5 minutes

Redis is also used directly through `StringRedisTemplate` for task view counters:

- `POST /api/tasks/{id}/views` increments Redis key `task:{id}:views`
- `GET /api/tasks/{id}/views` reads Redis key `task:{id}:views`
- `POST /api/tasks/{id}/views` also writes `lastViewedAt` into Redis hash `task:{id}:meta`
- `GET /api/tasks/{id}/meta` reads `lastViewedAt` from Redis hash `task:{id}:meta`
- The view counter is kept in Redis, not PostgreSQL

Redis Pub/Sub is used for simple task lifecycle notifications:

- `POST /api/tasks` publishes `TASK_CREATED:{id}` to channel `task-events`
- `DELETE /api/tasks/{id}` publishes `TASK_DELETED:{id}` to channel `task-events`
- `TaskEventSubscriber` listens to `task-events` and logs received messages
- Redis Pub/Sub messages are not durable; subscribers must be online when messages are published

## Project Structure

```text
task-management-api/
├── docker-compose.yml
├── README.md
├── task-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/business/project/taskmanagement/
│       ├── common/
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── exception/
│       ├── repository/
│       └── service/
└── notification-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/com/business/project/notification/
        ├── controller/
        └── dto/
```

## Task API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/tasks` | List all tasks |
| GET | `/api/tasks/{id}` | Get task by id |
| POST | `/api/tasks` | Create task |
| PUT | `/api/tasks/{id}` | Update task |
| DELETE | `/api/tasks/{id}` | Delete task |
| GET | `/api/tasks/{id}/views` | Get task view count from Redis |
| POST | `/api/tasks/{id}/views` | Increment task view count in Redis |
| GET | `/api/tasks/{id}/meta` | Get task metadata from Redis hash |

## Notification API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/notifications` | Receive and log a task notification |

## Run With Docker Compose

Create a local `.env` file from the example file:

```powershell
Copy-Item .env.example .env
```

Then update `.env` with your local development credentials if needed.

From the repository root:

```powershell
docker compose up -d --build
```

Dockerized task API URL:

```text
http://localhost:8082/api/tasks
```

Dockerized notification API URL:

```text
http://localhost:8083/api/notifications
```

PostgreSQL connection from host machine for local development:

```text
Host: localhost
Port: 5432
Database: taskdb
Username: see local .env
Password: see local .env
```

The `.env` file is ignored by Git. Do not commit real credentials.

Useful Docker Compose commands:

```powershell
docker compose ps
docker compose logs task-service
docker compose logs notification-service
docker compose logs postgres
docker compose logs redis
docker compose stop
docker compose up -d
docker compose down
```

Useful Redis checks:

```powershell
docker exec redis-cache redis-cli ping
docker exec redis-cache redis-cli keys "*"
```

Warning: this command removes the PostgreSQL volume and deletes database data:

```powershell
docker compose down -v
```

## Run Locally From IntelliJ

Start PostgreSQL, Redis, and notification-service with Docker Compose:

```powershell
docker compose up -d postgres redis notification-service
```

Then run `TaskManagementApiApplication` from IntelliJ.

Local API URL:

```text
http://localhost:8081/api/tasks
```

The local task-service uses the default datasource and service URLs from `application.yaml` unless environment variables are provided:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskdb
    username: ${SPRING_DATASOURCE_USERNAME:rambo}
    password: ${SPRING_DATASOURCE_PASSWORD:12345}

services:
  notification:
    url: ${NOTIFICATION_SERVICE_URL:http://localhost:8083}
```

When the application runs inside Docker, Docker Compose overrides these values with environment variables and uses:

```text
jdbc:postgresql://postgres:5432/taskdb
```

## Example Create Task Request

```http
POST /api/tasks
Content-Type: application/json
```

```json
{
  "title": "Learn Docker Compose",
  "description": "Run Spring Boot and PostgreSQL together",
  "completed": false
}
```

Example response:

```json
{
  "id": 1,
  "title": "Learn Docker Compose",
  "description": "Run Spring Boot and PostgreSQL together",
  "completed": false,
  "createdAt": "2026-06-10T18:36:29.562185",
  "updatedAt": "2026-06-10T18:36:29.562185"
}
```

## Learning Roadmap

Planned learning steps for this project:

- Improve CRUD API design
- Add unit and integration tests
- Improve Redis caching and cache serialization
- Replace synchronous REST notification flow with Kafka event publishing
- Add JWT authentication and authorization
- Add Flyway database migrations
- Improve Docker image build performance
- Add CI pipeline

## Notes

This project is intentionally built step by step as a learning project. Some configurations are development-oriented and will be improved as new backend concepts are introduced.
