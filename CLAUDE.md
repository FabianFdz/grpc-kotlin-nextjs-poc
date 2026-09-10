# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

Scaffold only — see the status banner at the top of `README.md`. Every
`.kt`/`.ts`/`.tsx` source file is an intentional placeholder with a `TODO`
comment describing exactly what belongs there; only config files
(`build.gradle.kts`, `package.json`, `buf.yaml`, etc.) are real. Real
implementation is tracked as epics — see "Planned work" below. Don't infer
behavior from a stub; check its `TODO` comment and the relevant epic
instead.

## Commands

### Environment

Java 21 / Kotlin 2.4.20 / Gradle 9.7.1 are managed via `mise` (global
config, not pinned inside this repo). Ensure
`~/.local/share/mise/shims` is on `PATH` (or run `eval "$(mise activate
bash)"`) before running any Gradle/Kotlin command — otherwise `java`,
`kotlinc`, and `./gradlew` fail with "Unable to locate a Java Runtime."

`buf` (proto codegen) is installed separately (Homebrew); not managed by
mise.

### Backend (Kotlin / Gradle)

```bash
./gradlew build                                    # both services: compile + test
./gradlew :inventory-service:build                 # single module
./gradlew :pricing-service:build
./gradlew :inventory-service:test --tests "com.poc.inventory.external.DummyProductClientTest"
```

The Gradle wrapper is pinned to **9.7.1** specifically because
`inventory-service/build.gradle.kts` sets `Test.failOnNoDiscoveredTests =
false` (a Gradle 9+-only API), needed to keep `test` green while
`DummyProductClientTest` has no `@Test` methods yet. Don't downgrade the
wrapper without accounting for that.

### Contracts / codegen (buf)

```bash
pnpm generate       # buf generate -> packages/contracts/generated/ts + generated/kotlin/ (gitignored, never commit)
buf lint            # validate proto/
buf build           # validate buf.yaml/buf.gen.yaml + module resolution
```

`proto/**/*.proto` files currently contain only header comments (no real
`message`/`service` definitions), so `buf lint` reporting "no package
defined" etc. is expected until epic E1 lands real definitions.

### Frontend (Next.js, apps/web)

```bash
pnpm dev                                 # dev server (apps/web, via workspace filter)
pnpm build                               # production build
pnpm --filter @poc/web lint              # next lint (no eslint config committed yet)
npx tsc --noEmit -p apps/web/tsconfig.json   # strict type-check, run from apps/web
```

### Full stack

```bash
docker compose up
```

The three `apps/*/Dockerfile`s are currently placeholders with no real
build steps — making this command actually work end-to-end is explicitly
epic E5's job, not done yet.

## Architecture

**Two build tools, one contract.** This is a pnpm workspace
(`apps/web`, `packages/contracts`) for the JS/TS side and a Gradle
multi-module build (`settings.gradle.kts`) for the two Kotlin services
(`apps/inventory-service`, `apps/pricing-service`) — they share no
tooling, only a generated-contract boundary produced by `buf`.

**Contract-first codegen.** `proto/{inventory,pricing}/v1/*.proto` is the
single source of truth. `buf.gen.yaml` fans out in one pass: ts-proto →
`packages/contracts` (consumed by `apps/web` via the pnpm workspace
protocol, never published to a registry) and protoc-gen-kotlin +
grpc-kotlin → `generated/kotlin` (each service is expected to add only its
own package, e.g. `com/poc/inventory/v1/**`, as a Gradle source dir — the
two services don't share generated sources despite one codegen pass).
Neither generated tree is committed; regenerate with `pnpm generate`.

**Two integration patterns, handled asymmetrically on purpose** — this
asymmetry is the architectural point of the PoC, fully reasoned out in
`README.md`'s "Service-to-service architecture" and "Async third-party
integration" sections (read those before touching either client):
- *Internal* (`inventory-service` → `pricing-service` via
  `PricingServiceClient.getPrice`): gRPC, short timeout, no retry,
  graceful degradation — `GetItemWithPricing` returns the item without
  pricing if pricing-service is slow or down, rather than failing.
- *External* (`inventory-service` → DummyJSON via
  `DummyProductClient.fetchProduct`): HTTP, longer timeout, retried with
  exponential backoff on 5xx/timeout only (never on 404), mapped to a
  sealed success/not-found/unavailable result. Unlike the internal path,
  `EnrichItem` fails the RPC outright on any non-success outcome —
  enrichment is that RPC's entire purpose, not supplementary data.

**No gRPC in the browser.** `apps/web` talks gRPC only server-side via
`@grpc/grpc-js` (Server Components for reads; Route Handlers/Server
Actions for the streaming and mutating RPCs), using the types generated
into `packages/contracts`.

## Planned work (epics)

Implementation is tracked as five epics with a strict dependency chain:
**E1** Contract & Codegen Foundation → **E2** Pricing Service → **E3**
Inventory Service (CRUD/streaming/`ReserveItem`/`GetItemWithPricing`) →
**E4** External Third-Party Integration (`EnrichItem`) → **E5** Next.js
Frontend & E2E Wiring. Definitions and current status live in
`docs/epics/epic-status.md` and `docs/epics/E{n}-*.md`. Managed by the
`sprint-runner` plugin (`epic-creator` skill drafted them; `/sprint` plans
and builds against them). Check `epic-status.md` before assuming what's
already implemented.
