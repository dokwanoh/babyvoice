# Baby Voice Bridge Design System

## 0. Research Log

- Embedded refs: shortlisted `intercom.md`, `apple.md`, `cal.md` -> picked `taste-skill.md` + `intercom.md` because the product needs a calm, conversational companion surface with strong hierarchy and friendly warmth, not a dashboard clone.
- UI-UX DB: 2 searches, 6 results viewed -> family-care palette leaned toward blue primary, green positive/status, and a restrained amber attention color; typography leaned toward a highly legible sans stack with technical secondary text.
- Lazyweb: skipped because the only available token flow required Lazyweb MCP Pro for screen research in this workspace.
- Imagen drafts: skipped because the product direction is already grounded in a concrete Layer B reference and product-specific palette research, and the first pass is being optimized for buildable Android code rather than concept exploration.

## 1. Atmosphere & Identity

Quiet, reassuring, and operational. The surface should feel like a well-kept kitchen counter rather than a dashboard: one glance, one action, one answer. The signature is conversational density with roomy touch targets, where the content reads as a calm family briefing instead of a technical report.

## 2. Color

### Palette

| Role | Token | Light | Dark | Usage |
|------|-------|-------|------|-------|
| Surface/primary | `--surface-primary` | `#FCFAF6` | `#0B0F14` | App background |
| Surface/secondary | `--surface-secondary` | `#F4F7F4` | `#121821` | Section bands, inactive surfaces |
| Surface/elevated | `--surface-elevated` | `#FFFFFF` | `#17212B` | Cards, sheets, popovers |
| Surface/tint | `--surface-tint` | `#EDF4FF` | `#102033` | Selected or active focus zone |
| Text/primary | `--text-primary` | `#0F172A` | `#F8FAFC` | Headlines, body |
| Text/secondary | `--text-secondary` | `#475569` | `#CBD5E1` | Supporting copy |
| Text/tertiary | `--text-tertiary` | `#64748B` | `#94A3B8` | Metadata, hints |
| Border/default | `--border-default` | `#D7E0DB` | `#2B3643` | Card outlines, dividers |
| Border/subtle | `--border-subtle` | `#E7EEEA` | `#1F2833` | Soft containment |
| Accent/primary | `--accent-primary` | `#2563EB` | `#60A5FA` | Primary actions, focus |
| Accent/primary-hover | `--accent-primary-hover` | `#1D4ED8` | `#93C5FD` | Hover/pressed primary action |
| Accent/secondary | `--accent-secondary` | `#16A34A` | `#4ADE80` | Positive state, successful sync |
| Accent/attention | `--accent-attention` | `#D97706` | `#F59E0B` | Listening, waiting, attention state |
| Status/error | `--status-error` | `#DC2626` | `#F87171` | Failure, permission denial |
| Status/info | `--status-info` | `#0EA5E9` | `#38BDF8` | Neutral information |

### Rules

- Surface hierarchy is primarily tonal, reinforced by thin borders.
- Blue is the action color. Green is success or positive status. Amber is reserved for listening, waiting, or "attention" states.
- Never introduce a color not in this table.

## 3. Typography

### Scale

| Level | Size | Weight | Line Height | Tracking | Usage |
|-------|------|--------|-------------|----------|-------|
| Display | 40px / 2.5rem | 700 | 1.12 | -0.02em | Briefing title |
| H1 | 32px / 2rem | 700 | 1.15 | -0.015em | Screen title |
| H2 | 24px / 1.5rem | 600 | 1.20 | -0.01em | Section headers |
| H3 | 20px / 1.25rem | 600 | 1.25 | 0 | Card titles |
| Body/lg | 18px / 1.125rem | 400 | 1.55 | 0 | Lead copy |
| Body | 16px / 1rem | 400 | 1.60 | 0 | Default text |
| Body/sm | 14px / 0.875rem | 400 | 1.50 | 0 | Secondary info |
| Caption | 12px / 0.75rem | 500 | 1.40 | 0.02em | Labels, metadata |

### Font Stack

- Primary: Android system sans stack with Korean fallback; prefer the platform font over a web font to preserve CJK clarity and avoid unnecessary loading.
- Mono: `Roboto Mono`, `monospace`

### Rules

- Body text never drops below 14px.
- Use the system font for Korean legibility and consistent device rendering.
- Long labels wrap instead of truncating when the information is important.

## 4. Spacing & Layout

### Base Unit

All spacing derives from a base of **4dp**.

| Token | Value | Usage |
|-------|-------|-------|
| `--space-1` | 4dp | Tight icon-text spacing |
| `--space-2` | 8dp | Inline groups |
| `--space-3` | 12dp | Dense controls |
| `--space-4` | 16dp | Standard padding |
| `--space-5` | 20dp | Card internals |
| `--space-6` | 24dp | Section spacing |
| `--space-8` | 32dp | Between cards |
| `--space-10` | 40dp | Between major blocks |
| `--space-12` | 48dp | Major separation |
| `--space-16` | 64dp | Page rhythm |

### Grid

- Max content width: 1280dp
- Breakpoints: compact < 600dp, medium 600-839dp, expanded >= 840dp
- Layout primitive: adaptive shell that collapses to one column on phones and opens into two panes on tablets

### Rules

- Tokenize intent, not browser mechanics.
- Use asymmetric spacing intentionally when it helps the reading path.

## 5. Components

### PrimaryActionButton
- **Structure**: icon optional + label, full-width on phone, compact on tablet
- **Variants**: briefing, speak, confirm, secondary
- **Spacing**: `--space-3`, `--space-4`
- **States**: default, hover, pressed, focus, disabled, loading
- **Accessibility**: 44dp minimum touch target, visible focus ring, clear label
- **Motion**: 120-160ms opacity/scale feedback
- **Layout**: cluster inside a top action rail

### SummaryCard
- **Structure**: title, value, provenance line, optional status chip
- **Variants**: feeding, sleep, diaper, freshness, provider
- **Spacing**: `--space-4`, `--space-5`
- **States**: default, stale, empty, error, masked
- **Accessibility**: semantic heading, state text not color-only
- **Motion**: subtle tonal shift on state change
- **Layout**: card in a responsive grid

### VoiceControlBar
- **Structure**: brief status text + listen button + stop button
- **Variants**: idle, preparing, listening, processing, speaking, permission-required, error
- **Spacing**: `--space-3`, `--space-4`, `--space-6`
- **States**: all machine states represented visibly
- **Accessibility**: announces status changes, large taps, no microphone without consent
- **Motion**: pulse only when actively listening
- **Layout**: anchored control bar

### ScenarioChipSet
- **Structure**: pill chips for demo states
- **Variants**: normal, empty, stale, syncing, permission-denied, multi-baby, error
- **Spacing**: `--space-2`, `--space-3`
- **States**: selected, unselected, disabled, focus
- **Accessibility**: each chip has readable text and role
- **Motion**: small scale/tonal feedback only
- **Layout**: wrap cluster

### FreshnessBanner
- **Structure**: icon, message, timestamp
- **Variants**: fresh, stale, syncing, failed
- **Spacing**: `--space-3`, `--space-4`
- **States**: visible, collapsed
- **Accessibility**: sentence explains freshness in words
- **Motion**: fade and slide in
- **Layout**: full-width banner at top of content

## 6. Motion & Interaction

### Timing

| Type | Duration | Easing | Usage |
|------|----------|--------|-------|
| Micro | 120-160ms | ease-out | Button press, chip toggle |
| Standard | 180-240ms | ease-in-out | Card state change, panel reveal |
| Emphasis | 280-360ms | cubic-bezier(0.16, 1, 0.3, 1) | Briefing entrance, two-pane transition |

### Rules

- Animate only transform and opacity.
- Every animation signals state or affordance.
- Respect reduced motion and keep controls usable when motion is disabled.

## 7. Depth & Surface

### Strategy

Choose: tonal-shift with thin borders.

| Level | Value | Usage |
|-------|-------|-------|
| Border default | 1dp solid `--border-default` | Cards and shells |
| Border subtle | 1dp solid `--border-subtle` | Soft grouping |

Depth is communicated through surface tone, not heavy shadow stacks.

## 8. Accessibility Constraints & Accepted Debt

### Constraints

- WCAG target: 2.2 AA
- Visible focus on every interactive control
- All key actions reachable by keyboard, touch, and TalkBack
- No content depends only on color
- Sensitive values can be masked

### Accepted Debt

| Item | Location | Why accepted | Owner / Exit |
|------|----------|--------------|--------------|
| No custom downloadable Korean font | app-wide | Preserve build simplicity and CJK rendering safety in the first pass | Revisit after baseline demo is stable |

