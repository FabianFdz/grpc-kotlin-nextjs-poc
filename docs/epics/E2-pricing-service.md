# E2 — Pricing Service (internal gRPC)

## Goal
Stand up pricing-service as a real, independently runnable gRPC server that
answers price lookups — the simpler of the two backend services, built
first so inventory-service has a live internal dependency to call against.

## Scope
- An in-memory pricing repository, seeded once at startup with sample
  entries (base price, optional discount, promotion label) — read-only for
  this epic; no RPC creates or mutates pricing data.
- `GetPrice` answered over gRPC: given a known item id, returns its base
  price, active discount (if any), and promotion label; for an id with no
  seeded pricing rule, returns a `NOT_FOUND` error rather than a placeholder
  price.
- A standalone entrypoint that starts the gRPC server and binds a
  configurable port, so the service can be run and queried on its own
  (e.g. via a gRPC client tool) without inventory-service or any other
  component present.
- Wiring pricing-service's build to compile against the `GetPrice` message
  and service types generated in E1 — this epic is what E1 deferred that
  wiring to.

**Explicitly out of scope for this epic:**
- Any RPC that creates, updates, or removes pricing rules — the repository
  is startup-seeded only.
- inventory-service's side of the integration (calling pricing-service,
  timeout/degradation behavior) — that's a separate epic; this epic only
  delivers a callable pricing-service.
- Any real database or persistence — in-memory only, resets on restart.

## Depends on
E1 — requires E1's real `GetPrice` message/service definition and
generated Kotlin stubs to exist before this epic's implementation can
compile against them.

## Priority
must-have — inventory-service's pricing integration has nothing to call
until this exists, and it's the simpler of the two services, making it the
right one to unblock first.

## To settle
- Exact seed data (how many sample items, what prices/discounts/promotion
  labels) — left to the Planner/ticket level as implementation detail, not
  a product decision.
- Configurable port/address scheme (env var name, default value) — a
  mechanism choice for the Architect, not decided here.
