# ADR-003: Local-First Privacy

The first implementation keeps data local by default.

Rationale:
- The product handles sensitive family data.
- Demo mode must work offline.

Decision:
- No analytics SDKs, no ads SDKs, no raw transcript persistence, no audio file storage.

