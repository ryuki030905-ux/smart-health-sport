# Smart Health Sport Architecture

## Project position

Smart Health Sport is a personal health and exercise management system built as a graduation project. The repository is organized as a local multi-service application with a Vue frontend, a Spring Boot backend, and a separate FastAPI-based AI service.

This document describes the current codebase structure and request flow. It is intended as a human-facing technical overview rather than an internal implementation checklist.

## Runtime architecture

The system is composed of three main runtime parts plus three storage layers.

| Layer | Technology | Role |
|---|---|---|
| Frontend | Vue 3, Vite, Element Plus, Pinia, ECharts | UI, route handling, forms, charts, AI assistant workflow |
| Backend API | Spring Boot, Spring Security, MyBatis-Plus | Authentication, domain APIs, statistics, admin features, AI orchestration |
| AI service | FastAPI, LangChain, Chroma, sentence-transformers | User-context assembly, RAG retrieval, model invocation |
| Primary storage | MySQL | Core application data |
| Cache and quota | Redis | Token blacklist, caches, AI rate limiting |
| Knowledge retrieval | Chroma + markdown files | AI knowledge base and local vector store |

## High-level request flow

### Standard business flow

1. The browser interacts with the Vue SPA.
2. The frontend sends requests to `/api`.
3. Vite rewrites `/api` to the Spring Boot backend path `/api/v1` in local development.
4. Spring Boot authenticates the request with JWT, performs business logic, and reads or writes MySQL and Redis as needed.
5. The backend returns a unified `Result` payload to the frontend.

### AI advice flow

1. The frontend AI page submits an advice request to the backend.
2. The backend checks AI quota and calls the FastAPI AI service.
3. The AI service requests current user health, exercise, and diet summaries from backend internal endpoints.
4. The AI service retrieves related markdown knowledge from a local Chroma vector store.
5. The AI service builds a prompt and calls an OpenAI-compatible model provider.
6. The generated advice is returned to Spring Boot and then to the frontend.

## Frontend architecture

The frontend is located in `health-sport-frontend/` and is built with Vue 3 and Vite.

### Main route groups

- `dashboard` - overview page
- `health` - health record management
- `exercise` - exercise records and plans
- `diet` - diet records
- `charts` - statistics and visualization
- `ai` - AI health assistant
- `admin/users` - user management for administrators
- `admin/dict` - exercise and food dictionary management for administrators

### Frontend responsibilities

- Maintain login state with Pinia
- Send authenticated requests with Axios
- Render charts and dashboard summaries
- Collect AI request context and display returned advice
- Handle role-based route protection in the client

## Backend architecture

The backend is located in `health-sport-backend/` and uses Spring Boot 3.x.

### Core backend modules

- **Auth** - register, login, logout, JWT handling
- **Health** - personal health record storage and derived indicators
- **Exercise** - exercise dictionary, exercise records, exercise plans
- **Diet** - food dictionary search and diet records
- **Statistics** - aggregated chart data and summaries
- **AI** - advice request entrypoint, quota handling, caching, service-to-service calls
- **Admin** - user status, AI limits, and dictionary management

### Security model

- JWT-based stateless authentication
- Public access for auth endpoints and exercise dictionary lookup
- Admin endpoints protected by role checks
- AI endpoints require authenticated users
- Internal endpoints currently exist for local AI-service integration

### Data model overview

Main domain tables include:

- `user`
- `health_record`
- `exercise_dict`
- `exercise_record`
- `exercise_plan`
- `food_dict`
- `diet_record`

The backend also calculates and exposes derived values such as BMI, body fat estimates, calorie intake, calorie burn, weight trends, and weekly exercise summaries.

## AI service architecture

The AI service is located in `ai-service/` and uses FastAPI.

### Main responsibilities

- Accept AI advice requests from Spring Boot
- Pull latest user health, exercise, and diet summaries from backend internal endpoints
- Build prompts from live user context
- Retrieve relevant knowledge from markdown documents through local vector search
- Invoke an OpenAI-compatible model endpoint
- Return structured advice to the backend

### Knowledge retrieval pipeline

- Markdown knowledge files are stored in `ai-service/knowledge/`
- Text is embedded with a HuggingFace BGE model
- Vectors are stored in a local Chroma database
- Retrieval context is combined with user-specific runtime data before model invocation

## Key calculations

The backend uses explicit health and calorie formulas in utility logic.

### BMI

\[
BMI = \frac{weight\,(kg)}{(height\,(m))^2}
\]

### Body fat estimate

The codebase uses a BMI-based body fat estimate derived from age and gender.

### Exercise calories

Exercise calories are derived from MET values, user weight, duration, and intensity.

### Diet calories

Diet calories are derived from consumed quantity and food dictionary calories-per-100g values.

## Local development defaults

Typical local assumptions in the current repository are:

- Frontend dev server: `5173`
- Spring Boot backend: `8080`
- FastAPI AI service: `8000`
- MySQL: `3306`
- Redis: `6379`
- OpenAI-compatible model endpoint: `127.0.0.1:8787`

These values reflect local showcase usage, not a production deployment template.

## Current architectural constraints

- The repository is optimized for local development and project demonstration
- The AI feature depends on multiple running services and external model access
- Internal and debug endpoints are still development-oriented surfaces
- Production hardening and deployment orchestration are not yet fully packaged in this repository
- Public release requires careful secret management and environment separation

## Suggested reading order

For a quick code-oriented orientation, these files are the best starting points:

- `health-sport-frontend/src/router/index.js`
- `health-sport-frontend/src/api/index.js`
- `health-sport-backend/src/main/java/com/healthsport/controller/`
- `health-sport-backend/src/main/java/com/healthsport/service/impl/AiServiceImpl.java`
- `ai-service/main.py`
- `ai-service/agent.py`
- `ai-service/rag.py`
