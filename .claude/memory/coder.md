# Coder memory

- **`buf breaking` is not silent against a comment-only placeholder proto
  file.** buf treats an empty/comment-only `.proto` as an implicit proto2
  file with no package/options, so replacing it with a real proto3
  definition reports several "changed" findings (syntax, package,
  java_package, java_multiple_files) even though nothing was ever a real
  contract before. Don't assume a design doc's "breaking check reports
  nothing" claim for this repo's placeholder files — verify it directly
  with `buf breaking --against '.git#branch=main'` before relying on it,
  especially when a ticket wires `buf breaking` into CI.
