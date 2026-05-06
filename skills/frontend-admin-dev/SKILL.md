---
name: frontend-admin-dev
description: Work in D:\个人\充电\练手项目\cms\frontend-admin for Vue 3 admin-console tasks: page fixes, form/list/dialog changes, pagination and filter wiring, admin API integration, request-wrapper behavior, login/token flow, Element Plus UI work, and admin-focused codereview.
---

# Frontend Admin Dev

Use this skill for `frontend-admin/` tasks.

## Stack

- Vue 3
- Vite
- Element Plus
- Axios

## Key Paths

- `src/views/`: admin pages
- `src/api/request.js`: preferred request wrapper
- `src/api/modules/`: business API modules
- `src/router/index.js`: route guard and login redirect flow

## Default Page Trace

1. Open the target page in `src/views`.
2. Find the API call in `src/api/modules`.
3. Check whether the page expects `res.data`, `res.data.records`, `res.data.total`, or `error.response.data`.
4. Confirm the backend returns that exact shape.
5. Verify loading state, empty state, success message, and error message behavior.

## Admin-Specific Gotchas

- This repo had split axios wrappers. Prefer `src/api/request.js`.
- If a page catches an error and shows its own `ElMessage`, pass `skipBusinessErrorMessage: true` to avoid duplicate toasts.
- Reset pagination when filters or period selectors change, or you can get false empty tables.
- Do not render placeholder strings like “直接访问” as clickable links.
- For business errors, preserve `error.response.data` so page-level code can branch on backend codes/messages.

## Common Work Patterns

- List pages: query form + table + pagination + dialog
- Mutations: confirm dialog + async request + success toast + reload list
- Tables often expect `records` and `total`
- Many pages manage state locally with `ref`/`reactive`; do not introduce extra global state without need

## Validation

- Build command:
  `npm run build` in `frontend-admin`

Use build validation after meaningful changes, especially request-wrapper or module API updates.

## Review Focus

- Filter/pagination parameter mismatch
- Duplicate error toasts
- Dialog state not reset
- Missing `finally` cleanup for loading
- Wrong field names from backend contracts
- Unsafe direct external-link rendering
