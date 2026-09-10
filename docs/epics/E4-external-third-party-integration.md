# E4 — External Third-Party Integration

## Goal
Demonstrate a resilient async integration with a real external API: fetch
supplementary product data from DummyJSON to enrich an inventory item,
handling the external dependency's failure modes explicitly rather than
letting them leak as raw exceptions.

## Scope
- A client for the DummyJSON product-lookup API that applies a timeout,
  retries transient failures (5xx / timeout) with backoff, and never
  retries a definitive "not found," mapping every outcome to an explicit
  success / not-found / unavailable result rather than throwing.
- `EnrichItem`: given an existing inventory item id, fetches that item's
  supplementary DummyJSON product data (the item id is reused directly as
  the DummyJSON product id — no separate mapping) and merges it into the
  stored item. An unknown local item id returns `NOT_FOUND`, consistent
  with how `GetItem`/`ReserveItem` already treat unknown ids. If the
  DummyJSON lookup itself comes back not-found or unavailable, the RPC
  fails with an explicit gRPC error — enrichment is this RPC's entire
  purpose, so it does not silently degrade to returning the item
  unenriched.
- Finishing the real proto message/service definition for `EnrichItem`
  (neither E1 nor E3 covers it) and wiring inventory-service's build
  against the generated stubs for it.
- The PoC's one and only test: coverage of the DummyJSON client's
  behavior across a successful lookup, a not-found response, and a
  repeatedly-failing upstream — all without making a real network call.

**Explicitly out of scope for this epic:**
- Any other test in the codebase — this remains the PoC's only tested
  component, by design.
- Retrying or handling failures for any RPC other than `EnrichItem`.
- Exposing enrichment through the frontend — this epic is inventory-service
  only.

## Depends on
E1, E3 — needs E1's codegen pipeline to add `EnrichItem`'s proto
definition against, and needs E3's inventory item repository/CRUD as the
thing `EnrichItem` looks up and merges into.

## Priority
must-have — the external async integration is one of the three
architectural pillars this whole PoC exists to demonstrate (alongside
internal service-to-service gRPC and the streaming/CRUD surface), not an
optional add-on.

## To settle
- Exact retry count/backoff shape (already loosely described as
  "exponential backoff, max 3 attempts" in prior scaffolding notes) — a
  mechanism detail for the Architect, not a product decision.
- Which specific gRPC error status `EnrichItem` returns for a DummyJSON
  not-found vs. an unavailable upstream (they may warrant different
  codes) — left to the Architect.
