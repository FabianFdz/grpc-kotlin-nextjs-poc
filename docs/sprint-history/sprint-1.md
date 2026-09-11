# Sprint 1 — E1: Contract & Codegen Foundation

## Shipped tickets

- **E1-T01** — Define minimal proving proto contracts. Real
  `message`/`service` definitions for `InventoryService.GetItem` and
  `PricingService.GetPrice`; rest of each RPC surface stays TODO, deferred
  to E2-E4.
- **E1-T02** — Generate compiling TypeScript contracts. `pnpm generate`
  produces type-checked ts-proto output into
  `packages/contracts/generated/ts`, exposed via subpath exports.
- **E1-T03** — Generate compiling Kotlin stubs. Added the Java plugins
  (`protocolbuffers/java`, `grpc/java`) alongside Kotlin's, plus a new
  `tools/codegen-verify` Gradle module that proves the generated
  Java/Kotlin compiles without touching either service's real build.
- **E1-T04** — Run buf lint/breaking in CI. Pinned `buf` 1.72.0 via
  `bufbuild/buf-setup-action`; CI now runs `buf lint` and `buf breaking`
  against real proto content instead of a placeholder echo.

## Notable decisions / ADRs

- [ADR-1](../adr/ADR-1.md) — generate Java alongside Kotlin; pin all five
  buf remote plugins to versions matching the runtime deps already
  declared in the Gradle/pnpm builds.
- [ADR-2](../adr/ADR-2.md) — verify generated Kotlin/Java in a temporary
  `:codegen-verify` module rather than wiring it into either service early.
- [ADR-3](../adr/ADR-3.md) — money as `int32 *_cents` + `currency_code`; no
  64-bit proto fields until `forceLong` is explicitly decided.
- [ADR-4](../adr/ADR-4.md) — `buf-setup-action` pinned to 1.72.0,
  `buf breaking` baselined against `origin/main`.

## Doc fixes made at sprint close

- Confirmed no stale `service Inventory`/`service Pricing` naming remains
  anywhere (proto files, README.md, CLAUDE.md all already use the
  `Service`-suffixed names `InventoryService`/`PricingService` required by
  `buf.yaml`'s STANDARD lint category) — flagged during the sprint, checked
  clean, no change needed.
- `CLAUDE.md` now states the buf `1.72.0` version pin directly (previously
  only `docs/epics/E1-contract-codegen-foundation.md` had it, despite
  ADR-4/CI comments claiming it lived in `CLAUDE.md`), and its codegen/
  architecture sections now describe the real five-plugin pipeline, the
  two generated trees, `tools/codegen-verify`, and CI's buf wiring instead
  of the old scaffold-only description.
- Created `docs/README.md` and `docs/architecture.md` (didn't exist
  before); root `README.md` status banner and setup steps updated to
  reflect E1 being done.

## Known issues / carry-overs

- `generated/java` and `generated/kotlin` are still not wired into either
  service's Gradle build — `tools/codegen-verify` is a deliberately
  temporary stand-in until E2/E3 do that wiring, at which point it should
  be deleted (ADR-2).
- CI still doesn't run `buf generate`, Gradle builds/tests, or the web
  build — those remain echo placeholders, out of scope until E2/E3/E5.
