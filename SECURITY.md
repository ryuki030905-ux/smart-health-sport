# Security Policy

## Scope

This repository is currently published as a local-development and project-showcase codebase. It is not presented as a production-ready healthcare platform.

The project processes health-related records such as body metrics, exercise history, and diet records. Any real deployment should treat that data as sensitive and perform additional security hardening before internet exposure.

## Reporting a vulnerability

Please do not open a public issue for a suspected security problem.

Instead:

1. Prepare a short report with reproduction steps, affected files or endpoints, and possible impact.
2. Contact the repository owner through GitHub private channels if available.
3. Include whether the issue affects only local development or could affect a deployed environment.

## Current expectations

- Secrets should never be committed to the repository.
- Internal and debug endpoints should be treated as local-development surfaces.
- Public deployment should use environment-based secrets, restricted origins, and network isolation.
- The AI feature should not be treated as medical advice or diagnosis.

## Out of scope

The following are known non-production assumptions in the current showcase version:

- local-development ports and service URLs
- local proxy assumptions for frontend development
- locally hosted or externally provided OpenAI-compatible model endpoint
- local MySQL and Redis setup
