# ADR-3 — Money as integer minor units; no 64-bit fields in v1 yet

- Status: accepted
- Date: 2026-09-10
- Sprint / tickets: Sprint 1 — E1-T01 (binding on E2, E3, E5)

## Context

`Pricing.GetPrice` is the first RPC in the repo to carry money, so its field
types set the precedent every later pricing/inventory RPC will follow. Two
decisions are entangled:

- **Representation.** `double` loses cents to binary floating point;
  `string` decimals push parsing and validation onto every consumer.
- **Width.** ts-proto maps 64-bit proto fields according to its `forceLong`
  option: `number` (default, precision-lossy above 2^53), `long` (adds a `long`
  runtime dependency and an awkward frontend type), or `string`. Picking one now
  would commit `apps/web`'s ergonomics before any frontend code exists — but
  using `int64` *without* picking one silently accepts the lossy default.

## Decision

Money travels as **integer minor units** (cents) in a dedicated `*_cents`
field, alongside an ISO 4217 `currency_code` string. `GetPriceResponse` uses
`int32 base_price_cents`, `int32 discount_percent` (0–100, 0 meaning none), and
`string promotion_label` (empty meaning none).

`int32` — not `int64` — because it is exact, maps to a plain `number` in
TypeScript and `Int` in Kotlin with no extra dependency, and its ~$21.4M ceiling
per line item is far beyond anything this PoC models.

**No proto field in this repo uses `int64`, `uint64`, `fixed64`, or `sfixed64`
until the ts-proto `forceLong` option is decided.** The first ticket that
genuinely needs 64-bit width owns that decision and records it as its own ADR.

## Consequences

- Arithmetic is exact; no currency rounding bugs.
- Consumers divide by 100 for display; that is presentation-layer work, done
  once in `apps/web`.
- The `int32` ceiling is a real, if distant, limit. Widening a field from
  `int32` to `int64` later is wire-compatible, but it is a generated-code
  breaking change for consumers, so it would come with the `forceLong` decision
  above rather than as a quiet edit.
- Naming is now a convention: any future money field is `<name>_cents` with a
  sibling or enclosing `currency_code`. E2 (pricing logic), E3
  (`GetItemWithPricing`), and E5 (display) all inherit it.
