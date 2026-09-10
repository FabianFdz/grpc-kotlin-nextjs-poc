# ADR-1 — Generate Java alongside Kotlin, and pin every buf remote plugin

- Status: accepted
- Date: 2026-09-10
- Sprint / tickets: Sprint 1 — E1-T02, E1-T03

## Context

`buf.gen.yaml` currently declares three unpinned remote plugins:
`community/stephenh-ts-proto`, `protocolbuffers/kotlin`, `grpc/kotlin`. Two
problems with that, both confirmed by generating and compiling the output of a
throwaway `pricing.v1` proto before writing this ADR:

1. **The Kotlin plugins do not emit message classes.**
   `protocolbuffers/kotlin` emits only DSL builders (`GetPriceRequestKt.kt`,
   `PricingKt.kt`) and `grpc/kotlin` emits only the coroutine wrapper
   (`PricingServiceGrpcKt.kt`), whose first import is
   `com.poc.pricing.v1.PricingServiceGrpc.getServiceDescriptor` — a class emitted
   by `grpc/java`. The Kotlin DSL likewise wraps
   `GetPriceRequest.newBuilder()`, emitted by `protocolbuffers/java`. With the
   current plugin set the generated Kotlin cannot compile at all, which makes
   E1-T03's acceptance criteria unreachable as configured.

2. **Unpinned remote plugins float to `latest`.** The current `protocolbuffers`
   latest line is protobuf 4.x, whose generated Java calls
   `com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(...)` — an
   API that does not exist in the `protobuf-java` 3.25.3 runtime both service
   modules declare. Floating plugins therefore silently break compilation on a
   day when nothing in this repo changed.

## Decision

Add the two Java plugins and pin all five to the versions of the runtime
libraries already declared in `apps/*/build.gradle.kts` and `apps/web/package.json`:

| Plugin | Pin | Output dir | Matching runtime |
|---|---|---|---|
| `buf.build/community/stephenh-ts-proto` | `v2.12.1` | `packages/contracts/generated/ts` | `@bufbuild/protobuf`, `@grpc/grpc-js` |
| `buf.build/protocolbuffers/java` | `v25.3` | `generated/java` | `protobuf-java:3.25.3` |
| `buf.build/grpc/java` | `v1.65.1` | `generated/java` | `grpc-*:1.65.1` |
| `buf.build/protocolbuffers/kotlin` | `v25.3` | `generated/kotlin` | `protobuf-kotlin:3.25.3` |
| `buf.build/grpc/kotlin` | `v1.4.1` | `generated/kotlin` | `grpc-kotlin-stub:1.4.1` |

Java output goes to its own `generated/java` root rather than into
`generated/kotlin`, so each tree stays honestly named and each is a clean
compiler source root. Both are already covered by the `generated/` entry in
`.gitignore`.

The rule going forward: **a plugin pin and its runtime dependency version are
changed together, in the same PR, never independently.**

## Consequences

- Codegen output is reproducible; a green build stays green without network
  luck.
- Two generated trees instead of one. Every consumer (the `:codegen-verify`
  module now, `inventory-service` / `pricing-service` in E2/E3) must add *both*
  `generated/java` and `generated/kotlin` as source roots, filtered to its own
  `com/poc/{service}/v1/**` package.
- `grpc/java` output is annotated `@javax.annotation.Generated`, which is not on
  the JDK 21 classpath, so any module compiling it needs
  `compileOnly("org.apache.tomcat:annotations-api:6.0.53")`. Without it javac
  fails with "package javax.annotation does not exist".
- Upgrading protobuf or grpc later is a deliberate, coordinated change (four
  files) rather than a drive-by bump.
