# Architecture

Current state as of sprint 1 (epic E1 done). Updated every sprint — check
`docs/epics/epic-status.md` if anything here seems ahead of what's actually
implemented.

## Two build tools, one contract

- pnpm workspace (`apps/web`, `packages/contracts`) for JS/TS.
- Gradle multi-module build (`settings.gradle.kts`) for the two Kotlin
  services (`apps/inventory-service`, `apps/pricing-service`).

They share no tooling — only a generated-contract boundary produced by
`buf` from `proto/`.

## Contract pipeline

`proto/{inventory,pricing}/v1/*.proto` is the single source of truth.
Today it defines one RPC per service: `InventoryService.GetItem`,
`PricingService.GetPrice` (the rest of each surface is TODO-commented,
deferred to E2-E4).

`buf generate` (`pnpm generate`) fans out to five pinned remote plugins —
see [ADR-1](adr/ADR-1.md):

| Plugin | Output |
|---|---|
| `community/stephenh-ts-proto` | `packages/contracts/generated/ts` |
| `protocolbuffers/java` | `generated/java` |
| `grpc/java` | `generated/java` |
| `protocolbuffers/kotlin` | `generated/kotlin` |
| `grpc/kotlin` | `generated/kotlin` |

Java is required alongside Kotlin: the Kotlin plugins emit only DSL
builders and coroutine stubs that reference the Java-generated message
classes and `*Grpc` descriptors. None of the three generated trees are
committed; regenerate with `pnpm generate`.

`packages/contracts` (TS) is consumed by `apps/web` via the pnpm workspace
protocol — never published to a registry. `generated/java` +
`generated/kotlin` are **not yet wired into either service's Gradle
build** — that's E2/E3's job. Until then,
[`tools/codegen-verify`](../tools/codegen-verify) (temporary, see
[ADR-2](adr/ADR-2.md)) proves the generated Java/Kotlin compiles on its
own: `./gradlew :codegen-verify:classes`.

`buf lint` (STANDARD category) and `buf breaking` (FILE category) run in
CI on every change, pinned to buf 1.72.0 — see [ADR-4](adr/ADR-4.md) and
`.github/workflows/ci.yml`.

Money fields are integer minor units (`*_cents` + `currency_code`), never
floats or unpinned 64-bit widths — see [ADR-3](adr/ADR-3.md).

## Two integration patterns, handled asymmetrically on purpose

Not yet implemented (E3/E4), but the contract and design are set:

- **Internal** (`inventory-service` -> `pricing-service`, `GetPrice`):
  gRPC, short timeout, no retry, graceful degradation —
  `GetItemWithPricing` returns the item without pricing rather than
  failing if pricing-service is slow or down.
- **External** (`inventory-service` -> DummyJSON, `EnrichItem`): HTTP,
  longer timeout, retried with backoff on 5xx/timeout only, mapped to a
  sealed result. `EnrichItem` fails outright on any non-success outcome —
  enrichment is its entire purpose, not supplementary data.

See the root [`README.md`](../README.md) for the full reasoning.

## No gRPC in the browser

`apps/web` will talk gRPC only server-side via `@grpc/grpc-js` (Server
Components for reads; Route Handlers/Server Actions for streaming and
mutating RPCs), using the types generated into `packages/contracts`. Not
implemented yet (E5).

## Epics

Implementation is tracked as five epics, strict dependency chain:
**E1** Contract & Codegen Foundation (done) -> **E2** Pricing Service ->
**E3** Inventory Service -> **E4** External Third-Party Integration ->
**E5** Next.js Frontend & E2E Wiring. See
[`epics/epic-status.md`](epics/epic-status.md).
