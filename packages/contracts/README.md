# @poc/contracts

TypeScript types generated from the `.proto` files in [`proto/`](../../proto)
via [buf](https://buf.build) and the [ts-proto](https://github.com/stephenh/ts-proto)
plugin. Consumed by `apps/web` as a versioned workspace package, so the
frontend and backend share a single source of truth for message and service
shapes instead of hand-maintained TypeScript interfaces drifting from the
Kotlin services.

## Status

Scaffold only. No generated code exists yet — `proto/` currently contains
header comments describing the intended RPC surface, not real message or
service definitions.

## Generating

Once the proto files have real definitions:

```bash
npm run generate --workspace @poc/contracts
```

This runs `buf generate` against the repo-root `buf.gen.yaml`, which also
emits the Kotlin stubs used by `inventory-service` and `pricing-service` in
the same pass. Output lands in `generated/ts/` (gitignored — regenerate
rather than hand-edit).

## Versioning

See the "Contract versioning" section of the [root README](../../README.md)
for how proto changes are expected to flow through `buf breaking` checks and
semver bumps of this package.
