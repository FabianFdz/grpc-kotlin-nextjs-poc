# Plan — E1: Contract & Codegen Foundation

## Sprint Breakdown
- Sprint 1: E1-T01, E1-T02, E1-T03, E1-T04

The whole epic fits in one sprint: it is a small, foundational pipeline with
a natural build order (real proto content, then each codegen target, then
CI wiring), and no ticket is large enough to need splitting further.

## Tickets

### E1-T01: Define minimal proving proto contracts — Sprint 1
**Description:** Replace the placeholder header-comment-only proto files
with real message and service definitions for exactly one representative
RPC per service: `Inventory.GetItem` (id in, item out) and
`Pricing.GetPrice` (item id in, price/discount/promotion out). This is the
minimal pair needed to exercise both codegen plugins end-to-end; the rest
of each service's RPC surface (CRUD, `WatchStock`, `ReserveItem`,
`EnrichItem`, `GetItemWithPricing`) stays as documented TODOs, deferred to
the epics that implement them.
**Acceptance Criteria:**
- `proto/inventory/v1/inventory.proto` defines a real `GetItem` RPC with
  request/response messages; the remaining RPCs stay documented as
  deferred TODOs (not implemented).
- `proto/pricing/v1/pricing.proto` defines a real `GetPrice` RPC with
  request/response messages.
- `buf lint` passes with no errors against both files.
- `buf build` succeeds (module resolves, no compile errors).
**E2E Flows:**
- Developer runs `buf lint` → no errors reported for either proto file
  (previously reported "no package defined" against the placeholder
  comments).
- Developer runs `buf build` → both proto packages resolve and compile
  successfully.

### E1-T02: Generate compiling TypeScript contracts from proto — Sprint 1
**Description:** Make `pnpm generate` produce real, compiling TypeScript
types and gRPC client stubs for `GetItem`/`GetPrice` into
`packages/contracts/generated/ts`, ready for `apps/web` to import via the
pnpm workspace (no registry publish).
**Acceptance Criteria:**
- `pnpm generate` runs ts-proto codegen and populates
  `packages/contracts/generated/ts` with types/stubs for both RPCs.
- The generated TypeScript compiles cleanly with no type errors.
- Generated output stays gitignored (never committed).
**E2E Flows:**
- Developer runs `pnpm generate` → `packages/contracts/generated/ts`
  is created/refreshed with `GetItem` and `GetPrice` types.
- Developer type-checks the generated output → zero type errors.

### E1-T03: Generate compiling Kotlin stubs from proto — Sprint 1
**Description:** Make `pnpm generate` (via `buf generate`) produce real,
compiling Kotlin message classes and grpc-kotlin stubs for
`GetItem`/`GetPrice` into `generated/kotlin`, split into each service's own
package tree (`com/poc/inventory/v1/**`, `com/poc/pricing/v1/**`). Proving
the generated code compiles is this ticket's job; wiring it into
`inventory-service`'s or `pricing-service`'s own Gradle build is explicitly
out of scope (deferred to the epic that implements real RPC handlers).
**Acceptance Criteria:**
- `pnpm generate` populates `generated/kotlin/com/poc/inventory/v1/**` and
  `generated/kotlin/com/poc/pricing/v1/**` with real generated sources
  (not empty/placeholder).
- The generated Kotlin compiles cleanly, verified by a standalone check
  that does not modify either service's real Gradle build/source sets.
- Generated output stays gitignored (never committed).
**E2E Flows:**
- Developer runs `pnpm generate` → both Kotlin package trees are
  populated with generated sources for `GetItem`/`GetPrice`.
- Developer runs the standalone compile-verification step → succeeds with
  no compile errors.

### E1-T04: Run buf lint/breaking in CI — Sprint 1
**Description:** Provision `buf` in CI (pinned version, not "latest") and
replace the `buf-breaking` job's placeholder echo in
`.github/workflows/ci.yml` with real `buf lint` and
`buf breaking --against '.git#branch=main'` runs against the real proto
content landed in E1-T01, so the pipeline actually gates future proto
changes.
**Acceptance Criteria:**
- CI installs a pinned `buf` version (documented in the workflow file).
- The CI job runs `buf lint` and `buf breaking` against real proto
  content instead of the placeholder echo.
- A CI run on this ticket's own PR passes (lint and breaking-check both
  green against the current `main` baseline).
**E2E Flows:**
- Developer opens this ticket's PR → the CI job runs `buf lint` and
  `buf breaking` and reports pass/fail instead of the old echo TODO.
- Developer pushes a proto change that removes/renames a field on a
  follow-up branch → `buf breaking` in CI fails, demonstrating the gate
  works (verification only — not committed as part of this ticket).

## Out of Scope
- Defining the remainder of each service's RPC surface (CRUD, `WatchStock`,
  `ReserveItem`, `EnrichItem`, `GetItemWithPricing`) — each is defined when
  the epic that implements it is planned.
- Wiring generated Kotlin stubs into `inventory-service`'s or
  `pricing-service`'s actual Gradle build/source sets.
- Publishing `@poc/contracts` to any npm registry.
- Any real RPC handler implementation (server or client logic) — this
  epic proves the codegen pipeline only.

## Open Questions
None — the epic's "To settle" items (minimal RPC pair, CI provisioning
approach) are resolved above: `GetItem` + `GetPrice` as the minimal proving
pair, and CI buf provisioning scoped into E1-T04. The exact buf version
pin and standalone Kotlin compile-verification mechanism are left to the
Architect/Coder as implementation details.
