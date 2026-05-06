---
name: frontend-client-dev
description: Work in D:\个人\充电\练手项目\cms\frontend-client for Vue 3 public-site tasks: resource list/detail flows, category/filter behavior, feedback and promotion integration, request-wrapper behavior, SEO-facing page fixes, and client-focused codereview.
---

# Frontend Client Dev

Use this skill for `frontend-client/` tasks.

## Stack

- Vue 3
- Vite
- Vue Router
- Axios

## Key Paths

- `src/views/`: public pages
- `src/api/request.js`: client request wrapper
- `src/api/*.js`: resource, feedback, promotion, friendlink APIs
- `src/router/index.js`: client route setup
- `src/components/`: reusable site components

## Current Page Surface

- `Home.vue`: homepage/resource list and category browsing entry
- `ResourceDetail.vue`: resource detail, download flow, and business-error handling
- `Help.vue`: static help content
- `Disclaimer.vue`: static disclaimer content

## Default Trace

1. Open the target page in `src/views`.
2. Find its API call in `src/api`.
3. Check whether the page reads `res.data`, `res.data.records`, or `error.response.data`.
4. Verify the backend controller/service returns the same shape.
5. Confirm loading, empty, error, and retry behavior.

## Client-Specific Gotchas

- `ResourceDetail.vue` depends on preserved backend business payloads for cases like repeat download or rate limiting.
- Public pages can break if the request wrapper collapses backend business failures into bare `Error` objects.
- Category/filter pages should reset paging or list state when query conditions change.
- External link, referer, or download target rendering should distinguish real URLs from plain text labels.
- Static pages still need UTF-8-safe edits; do not rewrite text only because the terminal displays mojibake.

## Review Focus

- Resource list/detail field mismatches
- Download button state and duplicate-submit windows
- Broken category, source, or keyword filters
- Missing fallback rendering for optional backend fields such as cover image, referer, or description
- Duplicate toast/message behavior when page code and request wrapper both handle errors

## Validation

- Build command:
  `npm run build` in `frontend-client`

Use build verification after request-wrapper, route, or page-contract changes.
