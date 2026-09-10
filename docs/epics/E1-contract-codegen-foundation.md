# E1 — Contract & Codegen Foundation

## Goal
Stand up a working, versioned contract pipeline — real proto definitions,
buf tooling, and generated code — that both Kotlin services and the
Next.js frontend can build against, proving the codegen path end-to-end
before any RPC is fully implemented.

## Scope
- Real message and service definitions for a minimal proving RPC surface
  (one representative RPC per service is enough) in
  `proto/inventory/v1/inventory.proto` and `proto/pricing/v1/pricing.proto`
  — just enough to exercise both codegen plugins for real, not the full RPC
  surface either proto file's header comments describe.
- `buf lint` and `buf breaking` running against real proto content (not
  placeholder header comments).
- `buf generate` producing valid, compiling TypeScript types (via ts-proto)
  into `packages/contracts`, consumed only within the pnpm workspace — no
  external npm registry publish.
- `buf generate` producing valid, compiling Kotlin stubs (via
  protoc-gen-kotlin + grpc-kotlin) for both services' proto packages.
- `buf` itself made runnable as part of the project's dev workflow —
  currently nothing in the repo provides or pins it.

**Explicitly out of scope for this epic** (deferred to later epics):
- Defining the rest of the RPC surface already sketched in the proto
  header comments (full CRUD, `WatchStock`, `ReserveItem`, `EnrichItem`,
  `GetItemWithPricing`) — each gets defined when the epic that implements
  it is planned.
- Wiring the generated Kotlin stubs into `inventory-service` /
  `pricing-service`'s Gradle builds so they compile against them — deferred
  to whichever epic implements real RPC handlers.
- Publishing `@poc/contracts` to any npm registry — stays workspace-local.

## Depends on
—

## Priority
must-have — no service or frontend work can consume real generated types
until this exists; every later epic that touches an RPC builds on this
pipeline actually working.

## To settle
- Which specific RPC(s) make up the "minimal proving subset" (e.g. pricing's
  `GetPrice` plus one inventory RPC) — left to the Planner, since it's a
  mechanism-level pick rather than a product outcome.
- Sequencing for defining the remaining RPC surface: presumably each
  remaining RPC gets its real proto definition when the epic implementing
  it is planned, rather than all at once — not decided here.
- `buf` (v1.72.0) is now installed locally via Homebrew, and `buf build`
  validates cleanly against the repo's existing `buf.yaml`/`buf.gen.yaml` —
  so local provisioning is no longer blocking. Still open: how `buf` gets
  provisioned in CI (version pin, install step in `.github/workflows/ci.yml`)
  — a tooling decision for the Planner/Architect, not a product outcome.
