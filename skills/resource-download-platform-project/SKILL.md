---
name: resource-download-platform-project
description: Work in the local repo D:\个人\充电\练手项目\cms. Use for repo-specific development, debugging, codereview, bug fixing, startup/build verification, API and page contract tracing, UTF-8/garbled text triage, and cross-layer fixes involving backend, frontend-admin, frontend-client, Docker, or project docs.
---

# Resource Download Platform Project

Use this skill when the task is about the `cms` repository as a whole or when you need cross-module context.

## Repo Map

- `backend/`: Spring Boot 2.7.18, Java 8, MyBatis-Plus, Spring Security, Redis, Quartz
- `frontend-admin/`: Vue 3 + Vite + Element Plus admin console
- `frontend-client/`: Vue 3 + Vite public site
- `monitoring/`: monitoring-related configs and supporting assets
- `docker-compose.yml`: MySQL, Redis, backend, both frontends
- `start.bat|start.sh|stop.bat|stop.sh`: local multi-service start/stop entrypoints
- `nginx-production.conf`: production reverse-proxy example
- Project docs: `README.md`, `PROJECT_SUMMARY.md`, `QUICKSTART.md`, `FILE_LIST.md`, `CODE_REVIEW_STANDARDS.md`, `CODE_REVIEW_CHECKLIST.md`, `BATCH_OPERATIONS_GUIDE.md`, `SCREENSHOTS.md`

## First Pass

1. Confirm whether the task belongs to `backend`, `frontend-admin`, `frontend-client`, or multiple layers.
2. Read the nearest controller/view/api module before proposing changes.
3. Trace the full contract when behavior crosses layers:
   `page/view -> api module -> axios wrapper -> controller -> service -> mapper/entity/result`
4. Prefer minimal, verifiable fixes over broad rewrites.

## Verified Commands

- Backend test:
  `D:\soft\apache-maven-3.9.12\bin\mvn.cmd test`
- Backend package:
  `D:\soft\apache-maven-3.9.12\bin\mvn.cmd clean package -DskipTests`
- Admin build:
  `npm run build` in `frontend-admin`
- Client build:
  `npm run build` in `frontend-client`

Use the explicit Maven path above instead of assuming `mvn` is on `PATH`.

## Runtime Entry Points

- Public site: `http://localhost:8080`
- Admin site: `http://localhost:8081`
- Backend API: `http://localhost:9090`
- API docs: `http://localhost:9090/doc.html`

## High-Value Review Areas

- Backend null-handling on aggregate/count results and legacy rows
- Frontend/backend contract mismatches around `res.code`, `res.data`, pagination fields, and business errors
- Admin/client axios wrapper inconsistencies
- Duplicate submit windows, stale status updates, and partial batch failures
- UTF-8 vs terminal mojibake: verify file bytes before rewriting text

## Project-Specific Gotchas

- `frontend-admin/src/api/index.js` and `frontend-admin/src/api/request.js` both existed; prefer the unified `request` flow unless you have a reason not to.
- Some pages catch errors and also show `ElMessage`; when that happens, pass `skipBusinessErrorMessage: true` through request config to avoid double toasts.
- For public download flow, preserve backend business payloads so pages can read `error.response.data.code`.
- Statistics-style pages can be wrong if backend paginates raw logs before grouping; verify aggregation order.
- Treat apparent Chinese “乱码” in terminal output carefully; confirm with UTF-8 reads before editing.

## Working Rules

- Use `apply_patch` for manual edits.
- Keep changes scoped.
- After fixes, run the smallest relevant verification command and report what was and was not verified.
- If the task is codereview mode, prioritize real bugs, regressions, and missing guards over style-only feedback.

## When to Open More Context

- Read `skills/backend-dev/SKILL.md` for backend-only work.
- Read `skills/frontend-admin-dev/SKILL.md` for admin-page work.
- Read `skills/frontend-client-dev/SKILL.md` for public-site work.
- Read `README.md` or `PROJECT_SUMMARY.md` only when you need product/module overview rather than a local code fix.
