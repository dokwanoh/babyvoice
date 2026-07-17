# Data Provenance

Every field in a snapshot keeps its origin and freshness.

## Core provenance model

- `DataOrigin` identifies where the value came from
- `RecommendationOrigin` identifies how a recommendation was produced
- `FieldValue<T>` carries the raw value, observed time, origin, confidence, import time, and freshness
- `SyncStatus` records whether the provider is synced, syncing, stale, unsupported, or failed

## Merge rule

When fields come from different sources, the app never erases that provenance. The UI can combine values into one briefing, but the underlying source is still visible per field.

## Freshness rule

If data is stale, the briefing says so explicitly. The UI never implies that an old snapshot is current.

## Export rule

Any user-selected export file shows the imported time in the UI. The original file is never uploaded or shared by this app.

## Notification rule

The lab flavor can read BabyTime notification fields only after the user enables Android notification access. The current target package is `yducky.application.babytime`, discovered from the connected Galaxy tablet's public package list. Values imported this way use `DataOrigin.NOTIFICATION`; fields that are not present in the notification remain empty.

## Accessibility widget rule

The lab flavor can read the visible BabyTime widget tree only after the user enables the AccessibilityService on the owned tablet. Parsed values from that tree use `DataOrigin.ACCESSIBILITY_WIDGET`. The raw node text is kept in memory only and never logged or exported.
