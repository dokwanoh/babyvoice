# Assumptions

- The repository starts effectively empty, so the first implementation can consolidate the requested boundaries into one Android app module.
- The user explicitly approved a lab-only AccessibilityService path on the owned Galaxy Tab A11+ to read the visible BabyTime widget tree. Demo and production remain gated.
- The widget text on the connected tablet is exposed through accessibility nodes, so OCR and MediaProjection are not needed for the current implementation.
- No real BabyTime export sample is available, so export parsing stays generic and defensive until a user-provided sample appears.
- On the connected Galaxy tablet, the installed BabyTime package is `yducky.application.babytime`. Lab notification ingestion is scoped to that package.
- The local machine should use Android SDK API 36 for both compile and target, because that is the highest stable platform currently installed in the local SDK setup.
- The app should prefer the platform Korean font stack over bundling a custom typeface.
- Demo flavor is the first finished user-facing path; lab and production remain scaffolded but not full-featured.
- Google Assistant custom intents are currently limited to `en-US`, so Korean voice invocation may require Google-side locale support before it can trigger the App Actions route reliably.
- On the current Samsung Galaxy Tab A11+ bench device (`SM-X230`, `ko-KR`), the live Google Assistant surface fell back to search instead of dispatching the `babyvoice://briefing` fulfillment during manual testing.
