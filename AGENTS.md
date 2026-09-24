# Accord Legacy agent guidance

## Migration phase branching

For every Accord/Gramophone migration phase, read [`docs/migration/WORKFLOW.md`](docs/migration/WORKFLOW.md) before creating or switching branches. Start the phase branch from the latest `feature/migration`, implement and verify only that phase, then merge it back into `feature/migration` after review. Keep `alpha` as the upstream base; do not implement migration phases directly on `alpha` or on the integration branch.

The project-level `plan.md` defines phase scope and acceptance criteria. Read it before beginning a phase.
