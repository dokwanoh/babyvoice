# ADR-001: Provider Boundary

We keep baby data access behind `BabyDataProvider` and provider-specific adapters.

Rationale:
- The UI should not know whether data comes from mock, export, notification, or partner API.
- Keeping a single boundary prevents silent provenance loss.

Decision:
- Use a provider interface with observable babies, observable snapshots, and refresh.

