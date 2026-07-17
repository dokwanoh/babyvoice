# App Actions

## Intent

App Actions are optional and must not block the baseline deep-link and shortcut experience.

## Current stance

- Keep the baseline app fully usable without App Actions.
- The app now exposes a custom Assistant capability that deep-links to `babyvoice://briefing`.
- Do not activate App Actions for a Designed for Families build without an explicit review.
- Document any Play Console review step separately from the app release.
- Assistant custom intents are currently limited to `en-US`; Korean voice invocation still depends on Google's supported locale behavior.
- Official Google Assistant user-invocation locales for `actions.intent.OPEN_APP_FEATURE` currently do not include `ko-KR`, so this tablet's live voice path is locale-gated until Google adds Korean support or a different assistant surface is adopted.
- For Samsung/Bixby-style invocation, the app treats the normal launcher `ACTION_MAIN` open as a full briefing request. This lets a user-created Bixby command that opens the app immediately produce the voice briefing without relying on Google App Actions.
- For Gemini/Google Assistant on the Galaxy Tab A11+, the verified live dispatch path is ordinary app opening by launcher label. The phrase `오케이 구글, 아기 상태 열어줘` opened the app. Audio output must be verified separately by observing `VoiceState.Speaking` / `말하는 중` and an audio-focus request from `com.babyvoice.bridge.demo.debug`.

## Current wiring

- `custom.actions.intent.OPEN_BRIEFING` launches the main briefing screen.
- The static `briefing` shortcut is bound to the same capability.
- The Quick Settings Tile opens the same `babyvoice://briefing` route and has been verified on the Galaxy Tab A11+ over wireless debugging.
- The fulfillment path reuses the existing deep-link handler in `MainActivity`.
- `MainActivity` also handles `ACTION_MAIN` by starting the briefing, so voice assistants that can only open an app still reach the voice output path.
- Launcher aliases expose additional app-open names: `아기 상태`, `베이비 브리핑`, `Baby Voice`, `아기 기록`, and `육아 기록`.
- The `demo` flavor additionally exposes `베이비 타임` for personal device testing. Production builds must not include this alias without separate written authorization.
- The `demo` flavor also publishes a `베이비 타임` static shortcut and Korean query pattern. This increases the chance that `오케이 구글, 베이비 타임` routes to the app, but final dispatch remains controlled by Google/Gemini.
- On the Galaxy Tab A11+ (`SM-X230`, `ko-KR`), `아기 브리핑 열어줘` initially stayed in Gemini, while `아기 상태 열어줘` successfully dispatched to the app after the alias was added.
- A follow-up real-device bug showed that app dispatch could happen before the selected baby and TTS engine were ready. `BabyVoiceViewModel` now waits briefly for both before auto-speaking.

## Non-goals

- No default assistant replacement
- No custom wake word
- No hidden background activation
