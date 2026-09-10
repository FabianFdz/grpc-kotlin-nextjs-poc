# ADR-2 — Verify generated Kotlin in a dedicated `:codegen-verify` module

- Status: accepted
- Date: 2026-09-10
- Sprint / tickets: Sprint 1 — E1-T03

## Context

E1-T03 must prove the generated Kotlin compiles, while E1's scope explicitly
defers wiring generated sources into `inventory-service` / `pricing-service`
(that belongs to the epic implementing real RPC handlers). So we need a compile
check that touches neither service's build file or source sets, and that a
developer and, later, CI can run as one command.

Options considered:

1. **Temporarily add source dirs to the two service modules** — violates the
   ticket's stated boundary and leaves half-wired builds behind.
2. **Invoke `kotlinc` from a shell script** — requires hand-assembling a
   classpath of protobuf/grpc/coroutines jars outside Gradle; fragile and
   duplicates dependency versions in a third place.
3. **A dedicated Gradle module that compiles the whole generated tree.**

## Decision

Add a Gradle module `:codegen-verify` at `tools/codegen-verify`, included from
`settings.gradle.kts`. It applies `kotlin("jvm")` (version inherited from the
root build), points its `main` source set at `generated/java` and
`generated/kotlin` (both whole trees, both services at once), declares the
protobuf/grpc/coroutines dependencies that generated code needs, and has no
sources of its own and no tests.

`./gradlew :codegen-verify:classes` is the verification command — it runs both
`compileJava` and `compileKotlin`, which is required: `compileKotlin` only
*analyses* the Java sources, it does not compile them, so a Java-side error
(such as the missing `javax.annotation` from ADR-1) surfaces only via
`compileJava`.

A guard task fails the build when the expected generated package directories are
absent or empty, and both compile tasks depend on it. Without the guard, a
missing `generated/` tree makes the source dirs simply empty and the compile
tasks pass vacuously — a false green, which is the one failure mode this module
exists to prevent.

## Consequences

- Both service builds stay untouched, as E1 requires.
- One extra Gradle module (config only, no committed sources) and one line in
  `settings.gradle.kts`.
- The module owns a second copy of the protobuf/grpc version numbers. It is
  bound by ADR-1's rule: those move together with the plugin pins.
- Verification depends on `pnpm generate` having been run first; the guard task
  reports that clearly instead of failing obscurely.
- Not wired into CI in this sprint — CI would first need to run `buf generate`
  (network + remote plugins). E1-T04 deliberately scopes CI to `buf lint` and
  `buf breaking`; running codegen in CI is a later hardening step.
- When E2/E3 wire generated sources into the real services, `:codegen-verify`
  becomes redundant and should be deleted rather than maintained in parallel.
