---
name: backend-dev
description: Work in D:\个人\充电\练手项目\cms\backend for Spring Boot backend tasks: controller/service/mapper tracing, security and JWT checks, transaction and batch-write fixes, SQL and pagination debugging, API contract changes, null-safety fixes, and backend-focused codereview.
---

# Backend Dev

Use this skill for `backend/` work only.

## Stack

- Spring Boot 2.7.18
- Java 8
- MyBatis-Plus 3.5.3.1
- Spring Security + JWT
- Redis
- Quartz
- Knife4j

## Code Layout

- `src/main/java/com/resource/platform/module/<biz>/controller`
- `src/main/java/com/resource/platform/module/<biz>/service`
- `src/main/java/com/resource/platform/module/<biz>/service/impl`
- `src/main/java/com/resource/platform/module/<biz>/mapper`
- `src/main/java/com/resource/platform/module/<biz>/entity|dto|vo`
- Shared layers: `common`, `config`, `exception`, `filter`, `util`

Main business modules:
- `category`
- `crawler`
- `feedback`
- `image`
- `promotion`
- `resource`
- `revenue`
- `system`
- `user`

## Default Trace Path

1. Start at controller entrypoint.
2. Follow to service interface and `service/impl`.
3. Inspect mapper query/update shape.
4. Confirm `Result<T>` / `PageResult<T>` contract.
5. Check whether frontend depends on exact field names or business error codes.

## Backend Review Checklist

- Null-safety on `selectCount`, aggregate maps, legacy nullable columns
- Transaction boundaries on multi-table writes
- Batch behavior: partial success, duplicate windows, idempotency
- Security: route exposure, role checks, JWT assumptions
- Mapper/query efficiency: N+1, loop queries, full scans
- Exception handling: no swallowed exceptions, no `System.out`, no `printStackTrace`

## Known Patterns in This Repo

- Pagination commonly uses `PageResult<T>`
- Many query services depend on `LambdaQueryWrapper`
- Public/business failures often return meaningful `code/message`; preserve them
- Old data can contain null counters or timestamps
- Defensive `Collectors.toMap(..., (a, b) -> a)` is safer where duplicate rows may surface through dirty data

## Verification

- Primary command:
  `D:\soft\apache-maven-3.9.12\bin\mvn.cmd test`
- Packaging:
  `D:\soft\apache-maven-3.9.12\bin\mvn.cmd clean package -DskipTests`

If you changed only backend Java, run backend verification before claiming the fix.

## Editing Guidance

- Prefer minimal service/controller patches.
- Rewrite whole methods only when localized edits are unsafe.
- Be careful with UTF-8-sensitive files; confirm actual file content before “fixing” apparent garbling.
