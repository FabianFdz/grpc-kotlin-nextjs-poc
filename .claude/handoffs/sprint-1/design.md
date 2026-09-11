# Sprint 1 Design — tickets E1-T01, E1-T02, E1-T03, E1-T04

## Tickets in Scope
E1-T01, E1-T02, E1-T03, E1-T04 (epic E1 — Contract & Codegen Foundation).

Build them in that order: T02/T03 need T01's real proto content, T04 needs it
as the baseline for `buf lint`.

ADRs written for this sprint: [ADR-1](../../../docs/adr/ADR-1.md) (plugin set +
version pinning), [ADR-2](../../../docs/adr/ADR-2.md) (`:codegen-verify`
module), [ADR-3](../../../docs/adr/ADR-3.md) (money representation),
[ADR-4](../../../docs/adr/ADR-4.md) (buf in CI). Read them before coding — they
carry the "why" that is deliberately not repeated below.

Every mechanism below was validated end to end in a throwaway probe before this
design was written: `buf lint` passes on the exact proto shapes given here, all
five pinned plugins generate, the generated TypeScript type-checks under
`strict` + `noUncheckedIndexedAccess`, and the generated Java + Kotlin compile
together with the exact dependency set listed. The Coder is executing a proven
path, not exploring one.

---

## Technical Approach

### E1-T01 — Minimal proving proto contracts

Rewrite both files with real definitions. Common to both: `syntax = "proto3";`,
package matching the directory (`inventory.v1` / `pricing.v1`),
`option java_package = "com.poc.{inventory|pricing}.v1";` and
`option java_multiple_files = true;`.

`java_package` is load-bearing, not cosmetic: it is what puts generated output
under `com/poc/{service}/v1/**` as the epic and both service `build.gradle.kts`
TODOs expect. Without it, output lands under `inventory/v1/**` and every
downstream path assumption breaks.

**Service names take the `Service` suffix** — `InventoryService`,
`PricingService` — because `buf.yaml` selects the `STANDARD` lint category,
whose `SERVICE_SUFFIX` rule requires it. This contradicts the `service Inventory
{ ... }` phrasing in the current proto header comments and in `README.md` /
`CLAUDE.md`; the lint rule wins, and the stale prose is flagged for the
Documenter below. `STANDARD` also forces the `GetItemRequest`/`GetItemResponse`
request/response naming used below, so the wrapper messages are mandatory rather
than stylistic.

`proto/inventory/v1/inventory.proto`:
- `message Item` — `string id = 1`, `string name = 2`, `string sku = 3`,
  `int32 quantity_available = 4`. Deliberately minimal; E3/E4 add fields (a
  purely additive, non-breaking change). No price field — pricing is the other
  service's concern, composed later by `GetItemWithPricing`.
- `message GetItemRequest` — `string item_id = 1`.
- `message GetItemResponse` — `Item item = 1`.
- `service InventoryService` with `rpc GetItem(GetItemRequest) returns
  (GetItemResponse);`. Document `NOT_FOUND` as the unknown-id outcome in a
  comment (no implementation in this epic).
- Preserve the existing header comment's inventory of deferred RPCs
  (`CreateItem`, `UpdateItem`, `DeleteItem`, `ListItems`, `WatchStock`,
  `ReserveItem`, `EnrichItem`, `GetItemWithPricing`) as TODO comments naming the
  epic each is deferred to. Do not define them.

`proto/pricing/v1/pricing.proto`:
- `message GetPriceRequest` — `string item_id = 1`.
- `message GetPriceResponse` — `string item_id = 1`, `int32 base_price_cents = 2`,
  `int32 discount_percent = 3`, `string promotion_label = 4`,
  `string currency_code = 5`. Field types per ADR-3; comment the sentinel
  meanings (`discount_percent = 0` → no discount, empty `promotion_label` → no
  promotion).
- `service PricingService` with `rpc GetPrice(GetPriceRequest) returns
  (GetPriceResponse);`.

No enums anywhere in this pass — `STANDARD` lint additionally requires
`_UNSPECIFIED` zero values and enum value prefixes, and the proving surface
needs none of it.

Verify with `buf lint` (must be silent) and `buf build`.

### E1-T02 — Compiling TypeScript contracts

`buf.gen.yaml`: pin the ts-proto plugin to
`buf.build/community/stephenh-ts-proto:v2.12.1`, keep the existing `opt` list
and `out` path unchanged.

The generated code imports `BinaryReader`/`BinaryWriter` from
`@bufbuild/protobuf/wire` and the grpc-js client/server types from
`@grpc/grpc-js` (ts-proto v2 dropped protobufjs for `@bufbuild/protobuf`). So
`packages/contracts/package.json` needs real dependencies where it currently has
none: `@bufbuild/protobuf` (`^2.14.1`) and `@grpc/grpc-js` pinned to the same
`^1.10.9` range `apps/web` already declares, plus `typescript` (`^5.5.0`) as a
devDependency. No `@types/node` and no `long` are needed — verified.

Its `main`/`types` currently point at `generated/ts/index.ts`, **a file ts-proto
never emits** (output mirrors the proto tree: `generated/ts/inventory/v1/inventory.ts`,
`generated/ts/pricing/v1/pricing.ts`), and no barrel can be committed there
because `generated/` is gitignored. Replace them with an `exports` map exposing
the two real files as subpaths (`./inventory/v1`, `./pricing/v1`) — see the
API section below.

Add `packages/contracts/tsconfig.json` for the type-check: `strict: true`,
`noUncheckedIndexedAccess: true`, `noEmit: true`, `types: []`, `skipLibCheck:
true`, `module`/`moduleResolution` matching `apps/web` (`esnext` / `bundler`),
including `generated/ts/**/*.ts`. Add a `typecheck` script running
`tsc --noEmit -p tsconfig.json`, so the acceptance criterion is one command:
`pnpm --filter @poc/contracts typecheck`.

Do not commit generated output; `.gitignore` already covers it. Confirm nothing
under `generated/` shows up in `git status` before opening the PR.

### E1-T03 — Compiling Kotlin stubs

Two parts, both per ADR-1 and ADR-2.

**Codegen.** In `buf.gen.yaml`, add `buf.build/protocolbuffers/java:v25.3` and
`buf.build/grpc/java:v1.65.1`, both `out: generated/java`, and pin the existing
Kotlin plugins to `buf.build/protocolbuffers/kotlin:v25.3` and
`buf.build/grpc/kotlin:v1.4.1`. The Java plugins are not optional: the Kotlin
plugins emit only DSL builders and the coroutine stub, which reference the
Java-generated message classes and `*Grpc` descriptors. Update the file's header
comment, which currently describes a Kotlin-only, single-tree layout.

**Verification module.** New Gradle module `:codegen-verify` at
`tools/codegen-verify`, included from `settings.gradle.kts` with an explicit
`projectDir` in the style of the existing two entries. Its `build.gradle.kts`:

- `plugins { kotlin("jvm") }` — version inherited from the root build (1.9.24).
- `main` source set: `java.srcDir` → `<rootDir>/generated/java`,
  `kotlin.srcDir` → `<rootDir>/generated/kotlin`. Resolve both from
  `rootProject`'s directory, not with `../..` relative paths. Whole trees, both
  services — this module verifies codegen, it does not model service boundaries.
- Dependencies (versions declared as local `val`s, matching the service modules
  and ADR-1): `io.grpc:grpc-kotlin-stub:1.4.1`, `io.grpc:grpc-protobuf:1.65.1`,
  `io.grpc:grpc-stub:1.65.1`, `com.google.protobuf:protobuf-java:3.25.3`,
  `com.google.protobuf:protobuf-kotlin:3.25.3`,
  `org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1`, and
  `compileOnly("org.apache.tomcat:annotations-api:6.0.53")` for the
  `@javax.annotation.Generated` annotation on grpc-java output.
- A guard task (e.g. `verifyGeneratedSourcesPresent`) that fails with an
  actionable message ("run `pnpm generate` first") when any of
  `generated/java/com/poc/inventory/v1`, `generated/java/com/poc/pricing/v1`,
  `generated/kotlin/com/poc/inventory/v1`, `generated/kotlin/com/poc/pricing/v1`
  is missing or empty. Both `compileJava` and `compileKotlin` depend on it.
  Without this the module passes vacuously when nothing has been generated.
- No sources of its own, no test dependencies, no test sources.

Verification command: **`./gradlew :codegen-verify:classes`** — it must be
`classes`, not `compileKotlin`, because `compileKotlin` only analyses Java
sources without compiling them, so Java-side errors would go unnoticed.

Do not touch `apps/inventory-service/build.gradle.kts` or
`apps/pricing-service/build.gradle.kts`, including their codegen TODO comments —
wiring is explicitly a later epic's job.

### E1-T04 — buf lint + breaking in CI

Edit `.github/workflows/ci.yml` only; leave every other placeholder echo step
alone (they belong to later epics).

- `actions/checkout@v4` gains `with: fetch-depth: 0` — required for a git-based
  breaking baseline.
- Add a `bufbuild/buf-setup-action@v1` step with `version: 1.72.0` and
  `github_token: ${{ github.token }}`, with a comment stating the pin must track
  the locally installed buf version documented in `CLAUDE.md`.
- Replace the single `buf-breaking` echo step with two run steps: `buf lint`, and
  `buf breaking --against ".git#branch=origin/main"`.

Rationale for the action choice and the `origin/main` baseline is in ADR-4. The
ticket is done when CI is green on its own PR.

---

## Data / Schema Changes

No database, no persistence layer in this epic. The schema changes are the wire
contracts themselves, defined in E1-T01 above:

- `inventory.v1`: `Item`, `GetItemRequest`, `GetItemResponse`.
- `pricing.v1`: `GetPriceRequest`, `GetPriceResponse`.

Both are new definitions replacing comment-only files, so `buf breaking` against
`main` reports nothing. Field numbers 1..n are consumed as listed; later epics
append rather than renumber, and never reuse a number.

---

## API / Interface Changes

**gRPC surface (new).**

| Service | RPC | Request | Response |
|---|---|---|---|
| `inventory.v1.InventoryService` | `GetItem` | `GetItemRequest{ item_id }` | `GetItemResponse{ Item item }` |
| `pricing.v1.PricingService` | `GetPrice` | `GetPriceRequest{ item_id }` | `GetPriceResponse{ item_id, base_price_cents, discount_percent, promotion_label, currency_code }` |

No handlers are implemented in this epic — the contract and its generated
bindings are the deliverable.

**Generated Kotlin/Java surface** (what E2/E3 will implement against):
`com.poc.{inventory,pricing}.v1` containing Java message classes
(`Item`, `GetItemRequest`, …), the Java `InventoryServiceGrpc` /
`PricingServiceGrpc` descriptors, Kotlin DSL builders (`item { }`,
`getPriceRequest { }`), and the coroutine stubs
`InventoryServiceGrpcKt.InventoryServiceCoroutineImplBase` /
`...CoroutineStub` (same pattern for pricing).

**`@poc/contracts` package interface (changed).** `main`/`types` →
`generated/ts/index.ts` is removed (that file is never generated) in favour of
subpath exports:

- `@poc/contracts/inventory/v1` → `./generated/ts/inventory/v1/inventory.ts`
- `@poc/contracts/pricing/v1` → `./generated/ts/pricing/v1/pricing.ts`

New scripts: `pnpm --filter @poc/contracts typecheck`.

**Build/CI interface (new commands).** `./gradlew :codegen-verify:classes`
verifies generated Kotlin/Java compiles (after `pnpm generate`). CI gains real
`buf lint` and `buf breaking` steps.

---

## Cross-Sprint Flags

Decisions made here that constrain later epics — call them out in review if a
later sprint contradicts them:

1. **`Service` name suffix** (E1-T01). E2/E3 implement
   `PricingServiceCoroutineImplBase` / `InventoryServiceCoroutineImplBase`, not
   `PricingCoroutineImplBase`. `README.md`, `CLAUDE.md`, and the proto header
   comments still say `service Inventory` / `service Pricing` — **Documenter:
   correct this at sprint close.**
2. **Two generated trees** (ADR-1). `generated/java` now exists alongside
   `generated/kotlin`. When E2/E3 wire codegen into the services, each module
   adds *both* roots filtered to its own `com/poc/{service}/v1/**` (use an
   `include` filter on the generated root — do not add the package directory
   itself as a source root) and needs the `annotations-api` `compileOnly`
   dependency. The "add the `com.google.protobuf` gradle plugin" TODOs in both
   service build files are wrong for this setup: buf does the codegen, Gradle
   only compiles it. **Documenter: `CLAUDE.md`'s codegen paragraph describes the
   Kotlin-only tree and needs the same correction.**
3. **Plugin pins and runtime versions move together** (ADR-1). A protobuf or
   grpc upgrade touches `buf.gen.yaml`, both service builds, and
   `tools/codegen-verify/build.gradle.kts` in one PR.
4. **No 64-bit proto fields until `forceLong` is decided** (ADR-3). The first
   ticket needing one owns that decision and its ADR. Money is always
   `*_cents` + `currency_code`.
5. **`@poc/contracts` ships raw TypeScript**, not compiled JS. E5 will need
   `transpilePackages: ["@poc/contracts"]` in `apps/web/next.config.ts`, and
   imports use the subpath form above.
6. **`:codegen-verify` is temporary** (ADR-2). Delete it in the epic that wires
   generated sources into the real services; do not maintain both.
7. **CI still does not run codegen, Gradle builds, tests, or the web build** —
   those echo placeholders are untouched by this sprint and belong to E2/E3/E5.
