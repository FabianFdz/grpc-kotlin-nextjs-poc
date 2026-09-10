# E3 — Inventory Service (CRUD + streaming + service-to-service)

## Goal
Stand up inventory-service as the core of the PoC: a real, independently
runnable gRPC server offering full item CRUD, live stock updates, stock
reservation with correct failure signaling, and a pricing lookup that
degrades gracefully when its internal dependency is unavailable.

## Scope
- Full item CRUD over gRPC: create, read (single + list), update, and
  delete inventory items.
- `WatchStock`: a live, server-streamed feed of stock-level changes a
  caller can subscribe to.
- `ReserveItem`: reserving stock for an item, with correct, distinct
  failure signaling for "item doesn't exist" vs. "not enough stock to
  reserve." Reservation must be race-free — two concurrent reservation
  attempts against the same item can never both succeed and oversell the
  available stock.
- `GetItemWithPricing`: attaching current price/promotion data to an item
  by calling pricing-service internally. When pricing-service is
  unavailable or too slow to answer, the item is still returned —
  without pricing data — rather than failing the whole request.
- Finishing the real proto message/service definitions for all of the
  above RPCs (E1 only committed to one minimal proving RPC for
  inventory-service; the rest of this surface is this epic's to define),
  and wiring inventory-service's build to compile against the generated
  stubs for them.

**Explicitly out of scope for this epic:**
- `EnrichItem` and any external third-party integration — a separate,
  later epic covers calling out to DummyJSON.
- Any frontend/UI work — this epic is inventory-service only, reachable by
  a gRPC client, not yet wired to `apps/web`.
- Authentication/authorization — no permission boundaries exist yet for
  any RPC in this epic.

## Depends on
E1, E2 — needs E1's remaining inventory proto/codegen work as a
prerequisite for this epic's own proto definitions to build on, and needs
E2's pricing-service actually running (not just planned) to verify both
`GetItemWithPricing`'s happy path and its graceful-degradation path.

## Priority
must-have — this is the core of the PoC; nothing downstream (frontend,
third-party integration) has a real inventory backend to build against
until this exists.

## To settle
- `WatchStock`'s exact streaming semantics (does a new subscriber get an
  initial snapshot of current stock before live updates, or only updates
  from the moment they subscribe?) — left to the Architect/Planner as a
  mechanism decision.
- Whether `DeleteItem` is a hard delete or something recoverable — for an
  in-memory PoC store this defaults to hard delete unless raised again.
