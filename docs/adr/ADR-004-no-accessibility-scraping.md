# ADR-004: No Unauthorized Accessibility Scraping

AccessibilityService-based scraping is excluded unless the user explicitly enables the lab-only widget reader on their own device.

Rationale:
- Unauthorized collection remains out of scope.
- The connected Galaxy Tab A11+ exposes the BabyTime widget text in the accessibility tree, so the lab build can read visible widget content without OCR or MediaProjection when the user enables the service.

Decision:
- Ship mock, export-file, notification, and official partner API paths for all flavors.
- Allow a lab-only AccessibilityService widget reader behind explicit user enablement.
- Keep demo and production free of unauthorized accessibility collection code.
