# Privacy Threat Model

## Threats

| Threat | Risk | Control |
|---|---|---|
| Other apps reading baby data | Device compromise or shared storage leak | Local storage only, no shared export cache, no background upload |
| Notification content exposure | Sensitive text in notifications or logs | Mask diagnostics, keep raw notifications out of disk and logs |
| TTS on lock screen | Nearby people hear private information | Respect user control, keep no automatic always-on speech |
| Lost tablet | Local data disclosure | Explicit cache clear, no cloud sync by default, device-level lock reliance |
| Multiple caregivers mixing babies | Wrong child briefing | Explicit baby selection and visible active profile |
| Wrong baby chosen | Incorrect answer to parent | Show selected baby on every main screen |
| Old data mistaken for fresh | Bad decision from stale state | Freshness banner and stale warning phrase |
| Network speech recognition | Transcript leaves device | On-device recognizer preferred; disclose network fallback before consent |
| Logs and crash reports | Sensitive record leakage | No names, amounts, or timestamps in logs or crash payloads |
| Malicious export ZIP | Path traversal or decompression abuse | Zip Slip defense, size limits, entry limits, malformed input handling |
| Unofficial API temptation | Legal and security risk | Explicit partner API gate only; no guessed endpoints |
| Accessibility widget reading | Visible widget text may include baby names and times | Lab-only service, user-enabled on the owned tablet, parse in memory only, do not log or persist raw node text |

## Default posture

Local-first, least privilege, and explicit user action for anything that moves data across a boundary.

## BabyTime notification bridge

The lab build limits notification ingestion to `yducky.application.babytime`. It does not store raw notification text, does not log baby names or notification bodies, and only keeps parsed domain fields in memory for the current app process. Production builds do not include the notification listener service.

## BabyTime widget reader

The lab build may also read the visible BabyTime widget tree when the user enables the AccessibilityService on the owned tablet. It does not upload or persist the raw node dump, and it only keeps parsed domain fields in memory for the current app process. Demo and production builds do not include the widget reader.
