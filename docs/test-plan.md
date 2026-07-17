# Test Plan

## Unit tests

- Korean voice intent parsing
- Time formatting and midnight edge cases
- Relative time phrases
- Milliliter formatting
- Sleep/wake calculation
- Empty, stale, multi-baby, and error states
- Provenance preservation
- Recommendation precedence
- Export ZIP safety and malformed input handling

## Integration tests

- Local fake-server adapter tests for the partner API skeleton
- Safe export parsing against synthetic fixture ZIP files

## UI tests

- Demo first launch
- Briefing button and speak button
- Mic permission denied path
- Deep links
- Rotation and tablet landscape layout
- Scenario switching
- Cache clear flow
- Provider display

## Manual QA

- Launch demo flavor on tablet and phone-sized emulators
- Verify briefing speaks in Korean
- Verify stale data warning appears when the scenario is old
- Verify deep links open the correct view
