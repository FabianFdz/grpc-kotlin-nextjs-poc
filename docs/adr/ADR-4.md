# ADR-4 — buf in CI: pinned setup action, breaking baseline `origin/main`

- Status: accepted
- Date: 2026-09-10
- Sprint / tickets: Sprint 1 — E1-T04

## Context

`.github/workflows/ci.yml` has a `buf-breaking` step that only echoes a TODO,
and nothing in the repo provisions `buf` for CI. Two sub-decisions:

- **How buf gets installed.** Homebrew is not available on the runner;
  downloading a release tarball by hand is a maintenance burden; the
  `bufbuild/buf-action@v1` composite runs lint + format + breaking + push behind
  one set of defaults, including a `buf format` check the repo has never applied.
- **What `buf breaking` compares against.** On a `pull_request` event
  `actions/checkout` produces a detached HEAD, and by default a shallow clone —
  neither `main` nor its history is necessarily present locally, so a git-input
  baseline can fail to resolve.

## Decision

Install buf with `bufbuild/buf-setup-action@v1`, pinned to **`version: 1.72.0`**,
matching the version developers have locally (recorded in `CLAUDE.md`), with
`github_token: ${{ github.token }}` to avoid anonymous API rate limits. Then run
`buf lint` and `buf breaking` as ordinary, visible shell steps rather than
delegating to the composite action — the workflow shows exactly which checks gate
proto changes, and adding `buf format --exit-code` later stays an explicit
choice.

Set `fetch-depth: 0` on `actions/checkout` and use
`buf breaking --against ".git#branch=origin/main"`. `origin/main` is chosen over
`main`: a full-history checkout always creates the remote-tracking ref, while the
local branch ref may not exist on a PR checkout. Both forms were verified
against buf 1.72.0 in this repo; the remote-tracking form is the one that also
holds in CI.

## Consequences

- Proto changes are gated for real: `buf lint` on style, `buf breaking` (FILE
  category, per `buf.yaml`) on wire compatibility with `main`.
- `fetch-depth: 0` makes checkout slower — negligible on a repo this size, and
  it is a hard requirement for any git-based breaking baseline.
- On `push` to `main`, `origin/main` is HEAD, so the breaking check is a no-op
  pass. The gate that matters runs on pull requests.
- The buf version now lives in two places (developer machines via Homebrew, CI
  via the pin). The workflow comment states this; a mismatch shows up as CI
  disagreeing with local `buf lint`, which is the intended signal.
- Only `lint` and `breaking` run in CI. `buf generate` is not run — see ADR-2's
  consequences.
