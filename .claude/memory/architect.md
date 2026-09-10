# Architect memory

- **Kotlin protobuf/gRPC codegen is never standalone.** `protocolbuffers/kotlin`
  emits only DSL builders and `grpc/kotlin` only the coroutine stub; both
  reference classes from `protocolbuffers/java` + `grpc/java`. Any design
  producing Kotlin stubs must generate the Java output too, put it on the same
  compile path, and add `compileOnly("org.apache.tomcat:annotations-api")` for
  grpc-java's `@javax.annotation.Generated`.
- **Pin buf remote plugin versions to the runtime library versions** declared in
  `build.gradle.kts` / `package.json` (protobuf-java `3.25.3` ↔ plugin `v25.3`).
  Unpinned plugins float to protobuf 4.x gencode, which calls
  `RuntimeVersion.validateProtobufGencodeVersion` and fails against a 3.x
  runtime. Treat plugin pin + runtime version as one atomic change.
- **`buf.yaml` uses the `STANDARD` lint category here**, so proto design is
  constrained before the Coder sees it: services need the `Service` suffix,
  RPCs need `XxxRequest`/`XxxResponse` wrapper messages, enums need
  `_UNSPECIFIED` zero values and value prefixes. Check the lint category before
  specifying any proto shape.
- **Probe before designing codegen/toolchain work.** Generating into the
  scratchpad and actually compiling/type-checking the output takes minutes and
  has repeatedly turned assumptions (missing plugins, absent `index.ts`,
  required deps) into verified facts. Never hand the Coder an unproven pipeline.
- **A "verify it compiles" ticket needs an emptiness guard.** Pointing a source
  set at a gitignored generated tree passes vacuously when the tree is absent —
  always pair it with a task that fails on missing/empty output.
