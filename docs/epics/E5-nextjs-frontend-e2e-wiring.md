# E5 — Next.js Frontend & E2E Wiring

## Goal
Give the whole PoC a real, browsable face: a Next.js frontend that
exercises every RPC across both backend services, and a single command
that brings the entire system — both Kotlin services and the frontend —
up together.

## Scope
- Frontend coverage for all 9 inventory-service RPCs (`CreateItem`,
  `GetItem`, `UpdateItem`, `DeleteItem`, `ListItems`, `WatchStock`,
  `ReserveItem`, `GetItemWithPricing`, `EnrichItem`) — every RPC must be
  reachable from the browser, but multiple RPCs may share a single page or
  route rather than each requiring a dedicated one (e.g. update/delete
  actions living on the same detail view as the item read).
- Deleting an item requires an explicit confirmation step before the
  irreversible `DeleteItem` call is made.
- `docker compose up` (or equivalent single command) actually builds and
  runs all three services together end-to-end — the placeholder
  Dockerfiles currently in the repo need to become real, working ones as
  part of this epic; no other epic owns that.
- A distinctive, polished visual treatment — not a default component-library
  look — including transitions/animations on navigation and interaction.
  This is a qualitative bar: judged by review, not by an automated
  checklist, and the specific visual language/animations are left to
  whoever implements it.

**Explicitly out of scope for this epic:**
- Any new backend RPC or behavior — this epic only consumes what E1–E4
  already deliver.
- Authentication/authorization — still none, consistent with every prior
  epic.

## Depends on
E1, E2, E3, E4 — needs the full generated contract, both services running,
and every RPC (including `EnrichItem`) actually implemented before the
frontend can wire against them and before docker-compose can bring
everything up together.

## Priority
must-have — the Next.js frontend is one of the three pillars the whole PoC
exists to demonstrate, and this epic is what makes the other four epics'
work actually visible and exercisable rather than only reachable via a
gRPC client tool.

## To settle
- Which RPCs get consolidated onto shared pages vs. their own dedicated
  route (e.g. does Create live on the list page or its own route) — left
  to the Planner/Architect as a page-structure decision, not a product
  outcome.
- Specific visual language, animation choices, and branding — deliberately
  left to whoever implements it, per the qualitative design bar above.
