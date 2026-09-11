# Docs index

Project overview, architecture, and history live here. The root
[`README.md`](../README.md) is the pitch and quickstart; this folder is the
detail underneath it.

## What this is

`grpc-kotlin-nextjs-poc` — a portfolio PoC: two Kotlin gRPC services
(inventory, pricing) sharing proto contracts with a Next.js frontend, plus
an internal gRPC call and an external HTTP integration (DummyJSON) to show
two different reliability postures. See the root README for the full
pitch and diagrams.

## Current status

Epic **E1 (Contract & Codegen Foundation) is done** — real proto contracts
for one RPC per service, and a working `buf generate` pipeline (TS + Java +
Kotlin). Everything else (both services' RPC handlers, the frontend,
Docker wiring) is still scaffold/TODO. Track per-epic status in
[`epics/epic-status.md`](epics/epic-status.md).

## Setup / run

```bash
pnpm install
pnpm generate        # buf generate -> packages/contracts/generated/ts + generated/java + generated/kotlin
./gradlew build       # both Kotlin services
```

See the root README's "Setup" section for the full command list and what's
not wired up yet.

## Map

- [`architecture.md`](architecture.md) — how the pieces fit together, kept
  current every sprint.
- [`adr/`](adr/) — architecture decision records (why, not what).
- [`epics/`](epics/) — epic definitions and status.
- [`sprint-history/`](sprint-history/) — one file per closed sprint: what
  shipped, decisions, carry-overs.
