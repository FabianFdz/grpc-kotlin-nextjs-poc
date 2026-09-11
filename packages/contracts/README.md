# @poc/contracts

TypeScript types generated from the `.proto` files in [`proto/`](../../proto)
via [buf](https://buf.build) and the [ts-proto](https://github.com/stephenh/ts-proto)
plugin. Consumed by `apps/web` as a versioned workspace package, so the
frontend and backend share a single source of truth for message and service
shapes instead of hand-maintained TypeScript interfaces drifting from the
Kotlin services.

## Status

`GetItem` / `GetPrice` codegen only (epic E1's minimal proving pair). Import
via the subpath exports below, not a package-root import.

```ts
import { GetItemRequest } from "@poc/contracts/inventory/v1";
import { GetPriceRequest } from "@poc/contracts/pricing/v1";
```

## Generating

```bash
pnpm generate
```

(from the repo root; runs `buf generate` against the repo-root `buf.gen.yaml`,
which also emits the Kotlin stubs used by `inventory-service` and
`pricing-service` in the same pass). Output lands in `generated/ts/`
(gitignored — regenerate rather than hand-edit). Type-check it with
`pnpm --filter @poc/contracts typecheck`.

## Versioning

See the "Contract versioning" section of the [root README](../../README.md)
for how proto changes are expected to flow through `buf breaking` checks and
semver bumps of this package.
