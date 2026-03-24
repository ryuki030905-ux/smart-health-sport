# Contributing

Thanks for taking interest in this project.

## Project status

This repository is currently maintained primarily as a graduation-project showcase and reference implementation. Contributions are welcome, but the main goal is to keep the repository understandable, safe to publish, and aligned with the existing architecture.

## Before contributing

- Read `README.md` for the project overview
- Read `ARCHITECTURE.md` for the current system structure
- Avoid committing secrets, local environment files, or generated artifacts
- Keep changes focused and easy to review

## Contribution guidelines

- Prefer small, single-purpose pull requests
- Update documentation when behavior, setup, or architecture changes
- Follow existing naming and module patterns in each service
- Keep local-only assumptions out of tracked files when possible

## What is especially helpful

- Documentation improvements
- Security hardening suggestions
- Better developer setup guidance
- Tests for important business or AI integration behavior
- UI polish that improves project presentation

## What to avoid

- Committing `.env`, `venv`, `node_modules`, build artifacts, or IDE files
- Mixing unrelated refactors into a single pull request
- Adding production claims that the current architecture does not support
