# ADR-006: Lab Widget Accessibility Reader

The lab flavor may read the visible BabyTime widget tree through an AccessibilityService after explicit user enablement on the user's own tablet.

Rationale:
- The user asked for actual widget reading instead of inference from notifications.
- The connected Galaxy Tab A11+ exposes BabyTime widget text nodes through accessibility, so no OCR or MediaProjection is required.
- The app can keep the raw text in memory only and convert it immediately into domain snapshots.

Decision:
- Add a lab-only AccessibilityService that serializes visible widget nodes and parses them into `BabyCareSnapshot`.
- Keep raw accessibility text out of disk and logs.
- Leave demo and production free of the accessibility service.
