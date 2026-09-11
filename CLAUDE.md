# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

**E1 (Contract & Codegen Foundation) is done** — see the status banner at
the top of `README.md`. `proto/**/*.proto` define real messages/services
for one RPC per service (`GetItem`, `GetPrice`), and `pnpm generate`
produces compiling TypeScript, Java, and Kotlin from them. Every
`.kt`/`.ts`/`.tsx` *service/frontend* source file is still an intentional
placeholder with a `TODO` comment — no RPC handler, UI component, or
external API call is implemented yet. Real implementation is tracked as
epics — see "Planned work" below. Don't infer behavior from a stub; check
its `TODO` comment and the relevant epic instead.

## Commands

### Environment

Java 21 / Kotlin 2.4.20 / Gradle 9.7.1 are managed via `mise` (global
config, not pinned inside this repo). Ensure
`~/.local/share/mise/shims` is on `PATH` (or run `eval "$(mise activate
bash)"`) before running any Gradle/Kotlin command — otherwise `java`,
`kotlinc`, and `./gradlew` fail with "Unable to locate a Java Runtime."

`buf` (proto codegen) is installed separately (Homebrew), pinned to
**1.72.0** — not managed by mise. CI installs the same pin via
`bufbuild/buf-setup-action` (see `.github/workflows/ci.yml`); keep the two
in sync.

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
pnpm generate                    # buf generate -> packages/contracts/generated/ts + generated/java + generated/kotlin (gitignored, never commit)
buf lint                         # validate proto/ (STANDARD category, see buf.yaml)
buf build                        # validate buf.yaml/buf.gen.yaml + module resolution
pnpm --filter @poc/contracts typecheck   # tsc --noEmit over the generated TS
./gradlew :codegen-verify:classes        # compiles generated Java + Kotlin (run pnpm generate first)
```

`buf.gen.yaml` fans out to five pinned remote plugins (ADR-1): ts-proto,
`protocolbuffers/java`, `protocolbuffers/kotlin`, `grpc/java`, `grpc/kotlin`.
Java is required alongside Kotlin — the Kotlin plugins emit only DSL
builders and coroutine stubs that reference the Java-generated message
classes and `*Grpc` descriptors. `:codegen-verify` (`tools/codegen-verify`,
ADR-2) is a temporary Gradle module that proves the generated Java/Kotlin
compiles without wiring it into either service's real build — delete it
once E2/E3 do that wiring.

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
single source of truth, currently one representative RPC per service
(`GetItem`, `GetPrice` — the rest of each surface is TODO-commented in the
proto files, deferred to E2-E4). `buf.gen.yaml` fans out in one pass across
five pinned plugins (ADR-1): ts-proto → `packages/contracts/generated/ts`
(consumed by `apps/web` via the pnpm workspace protocol, never published to
a registry), and `protocolbuffers/java` + `grpc/java` + `protocolbuffers/kotlin`
+ `grpc/kotlin` → `generated/java` and `generated/kotlin` (two trees, both
split per package, e.g. `com/poc/inventory/v1/**` — the Kotlin plugins emit
only DSL builders/coroutine stubs, so the Java classes they reference must
be generated too). Neither service's Gradle build consumes these trees yet
— that wiring is E2/E3's job; today `tools/codegen-verify` (ADR-2, temporary)
proves the generated Java/Kotlin compiles standalone. None of the three
generated trees are committed; regenerate with `pnpm generate`.
`buf lint`/`buf breaking` run in CI (ADR-4) against every proto change.

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
**E1** Contract & Codegen Foundation (done) → **E2** Pricing Service → **E3**
Inventory Service (CRUD/streaming/`ReserveItem`/`GetItemWithPricing`) →
**E4** External Third-Party Integration (`EnrichItem`) → **E5** Next.js
Frontend & E2E Wiring. Definitions and current status live in
`docs/epics/epic-status.md` and `docs/epics/E{n}-*.md`. Managed by the
`sprint-runner` plugin (`epic-creator` skill drafted them; `/sprint` plans
and builds against them). Check `epic-status.md` before assuming what's
already implemented.
