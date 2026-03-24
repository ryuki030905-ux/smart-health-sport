# Smart Health Sport

Smart Health Sport is a personal health and exercise management system built as a graduation project. It combines daily health records, exercise and diet tracking, chart-based analytics, role-based administration, and an AI assistant that generates personalized advice from current user data plus a local knowledge base.

## Features

- Health record management with BMI and body fat related indicators
- Exercise logging, exercise plans, and calorie burn calculation
- Diet logging with food dictionary lookup and calorie intake calculation
- Dashboard and chart views for weight, calorie balance, and weekly exercise
- Admin tools for users, AI daily limits, exercise dictionary, and food dictionary
- AI health assistant backed by a separate FastAPI service and markdown-based RAG knowledge

## System overview

This repository contains three local services:

| Part | Tech | Responsibility |
|---|---|---|
| Frontend | Vue 3, Vite, Element Plus, Pinia, ECharts | User interface, route handling, charts, AI assistant page |
| Backend | Spring Boot, Spring Security, MyBatis-Plus, MySQL, Redis | Authentication, business APIs, statistics, admin APIs, AI orchestration |
| AI service | FastAPI, LangChain, Chroma, sentence-transformers | Retrieve user context, run RAG, call OpenAI-compatible model, return advice |

For a deeper explanation of module boundaries and request flow, see [ARCHITECTURE.md](./ARCHITECTURE.md).

## Main modules

- User authentication and role-based access control
- Health records
- Exercise records and plans
- Diet records and food lookup
- Statistics dashboard
- AI advice generation
- Admin management

## Local development topology

This project is currently organized for local development and showcase usage, not turnkey public deployment.

| Service | Default port | Notes |
|---|---:|---|
| Frontend dev server | 5173 | Proxies `/api` to the Spring Boot backend |
| Spring Boot backend | 8080 | Main REST API |
| FastAPI AI service | 8000 | Receives AI advice requests from backend |
| MySQL | 3306 | Main application database |
| Redis | 6379 | Cache, AI quota, token blacklist |
| OpenAI-compatible endpoint | 8787 | Referenced by the AI service example config |

## Repository status

This repository has been cleaned for public visibility, but it still reflects a local-development architecture.

- Real secrets have been removed from tracked files
- Example configuration is provided for the AI service
- Backend configuration now uses environment variable placeholders
- Architecture documentation has been refreshed to match the current codebase

## Setup note

This repository is published primarily as a project showcase. It is possible to run locally with MySQL, Redis, the Java backend, the Python AI service, and the frontend dev server, but the repository does not currently guarantee one-command startup or production-ready deployment.

To experiment locally, prepare these prerequisites first:

- Java 17
- Node.js and npm
- Python 3.11+ recommended
- MySQL 8
- Redis
- An OpenAI-compatible model endpoint or local alternative for the AI service

Use these files as starting points:

- `health-sport-backend/src/main/resources/application.yml`
- `health-sport-backend/src/main/resources/application-example.yml`
- `ai-service/.env.example`
- `health-sport-frontend/.env.example`
- `health-sport-frontend/vite.config.js`

## AI feature notes

The AI assistant is not a standalone chatbot. The request path is:

1. Frontend sends advice request to Spring Boot
2. Spring Boot checks quota and forwards the request to the AI service
3. AI service pulls current health, exercise, and diet context from backend internal endpoints
4. AI service combines live user context with markdown knowledge retrieved from a local Chroma vector store
5. AI service calls an OpenAI-compatible model endpoint and returns generated advice

This means the AI feature depends on more than one process and more than one storage layer.

## Limitations

- Current configuration is local-development oriented
- No one-command containerized setup is included yet
- AI capabilities depend on an external or local model endpoint
- Some internal and debug surfaces are still intended only for local development
- This project is for educational and engineering demonstration purposes, not medical diagnosis or treatment

## Security and privacy

This project handles health-related records. It should be treated as sensitive data in any real deployment. Security hardening, deployment isolation, and production-specific secret management should be completed before exposing this stack publicly on the internet.

Please see [SECURITY.md](./SECURITY.md) before reporting vulnerabilities or deploying this project outside a local environment.

## License

This repository is released under the MIT License. See [LICENSE](./LICENSE).
