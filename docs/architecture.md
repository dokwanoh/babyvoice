# Architecture

## Shape

The first build uses one Android application module with package-level boundaries that mirror the requested structure:

- `core/model`
- `core/common`
- `core/voice`
- `core/ui`
- `data/mock`
- `data/export`
- `data/notification`
- `data/partner-api`
- `feature/briefing`
- `feature/settings`
- `integration/assistant`

This keeps the Gradle surface small while preserving the responsibility boundaries in code.

## Data flow

1. The selected flavor chooses the default provider.
2. The provider emits babies and snapshots as `Flow`.
3. The briefing formatter converts the snapshot into Korean text.
4. The TTS wrapper speaks the formatted briefing.
5. The voice intent parser routes one-shot speech commands.
6. Settings and scenario state are persisted locally.

## Why not multi-module yet

The codebase has no existing Android structure to preserve. A one-module bootstrap reduces plugin and dependency complexity, lets the demo path ship faster, and still leaves the boundary names intact for later extraction if the project grows.

## Extension points

- Provider switching at the repository boundary
- Pluggable export parsing
- Notification diagnostics only in `lab`
- Partner API adapter only in `production`
- Deep link routing and assistant entry points on the UI boundary

