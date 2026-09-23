# Migration branch workflow

`feature/migration` is the long-lived integration branch for the Accord/Gramophone migration. It is based on Accord's `alpha` branch. Do not implement migration phases directly on `alpha` or on `feature/migration`.

For each phase:

1. Start from the latest `feature/migration` and create a phase branch, for example `migration/phase-2-build-baseline`.
2. Implement only that phase, keeping its changes and commits scoped to its deliverable.
3. Verify the phase against its acceptance criteria and review the diff.
4. Merge the completed phase branch back into `feature/migration` after it is approved. Keep `feature/migration` as the accumulated migration state for the next phase.
5. Remove the phase branch after the merge has been pushed and confirmed.

Phase 1 was completed on `migration/phase-1-inventory` and fast-forwarded into `feature/migration`. The phase branch is temporary; `feature/migration` is the continuing base for subsequent migration work. Keep this document and the root migration plan in sync if the branching policy changes.
