# Phase 2.4 branding review

Visual owner approval is pending. [Implementation report](../../BRAND_SPLASH_REPORT.md) · [Asset provenance](../README.md) · [Approved specification](../../BRAND_SPLASH_SPEC.md).

The [splash concept](../references/emerald_3_ball_scorekeeper_splash_screen.png) and [Play concept](../references/3_ball_scorekeeper_play_screen.png) informed brand hierarchy only. Their oversized shield artwork, generated studio representations and alternative controls are not production assets. The app uses the supplied official ND files and the existing native red-ball vector.

## Actual Android 9 / API 28

Unedited device screencaps at 360 × 800 pixels / 160 dpi, software CPU and SwiftShader graphics. Before startup/History/Settings were captured before this phase's in-place APK update. Before Live, both Setup states and both Master states are copied unchanged from the Phase 2.3 emulator gallery. Live depicts the exact active Novice game retained for this task, as confirmed by serialized-payload equality; Setup shows the same default/native Master configuration fields. The guest status-bar clock differs between captures.

The first frame initially labelled Live captured the real starting window instead, so it is explicitly labelled startup below. Actual Live was captured after initialization. No artificial splash hold was introduced. Android 9 has no Android 12 system splash API; these are legacy starting windows.

| State | Phase 2.3 | Phase 2.4 |
| --- | --- | --- |
| startup | ![Before startup](api28-before/startup.png) | ![After startup](api28-after/startup.png) |
| live | ![Before live](api28-before/live.png) | ![After live](api28-after/live.png) |
| history | ![Before history](api28-before/history.png) | ![After history](api28-after/history.png) |
| settings | ![Before settings](api28-before/settings.png) | ![After settings](api28-after/settings.png) |
| Default Setup | ![Before Setup](api28-before/setup.png) | ![After Setup](api28-after/setup.png) |
| Selected Master | ![Before Master Setup](api28-before/setup-master.png) | ![After Master Setup](api28-after/setup-master.png) |
| Master zero | ![Before Master zero](api28-before/master-idle.png) | ![After Master zero](api28-after/master-idle.png) |
| Master negative | ![Before Master negative](api28-before/master-negative.png) | ![After Master negative](api28-after/master-negative.png) |

The API 28 captures show the Phase 2.4 header/legacy startup implementation; subsequent final changes add the Android 12 branding slot and clean up resource placement/unused text without changing API 28 appearance. The active game and three history records were compared exactly after cold launch and the tab walkthrough. No app uninstall, clear-data, backup-policy change or direct database edits were used. The final-APK walkthrough then completed/archived the prior sample Novice game through normal controls and started a new Master sample for dense-layout review, retaining all earlier history.

### Actual responsive review

| Larger logical display | Large text / compact official branding |
| --- | --- |
| ![Larger window](api28-after/live-large.png) | ![150 percent fonts](api28-after/live-large-font.png) |

The larger-window capture uses a `wm size 420x900` logical display override on the same AVD. The large-text capture uses 150% system font scale on the normal 360 × 800 display. These are unedited 360 × 800 physical screencaps; the 420 × 900 logical override is scaled/letterboxed by the emulator display, rather than being a native 420-pixel capture. Both display size and font scale are restored after review. No phone-frame/composite or resized screenshot is used.

### Genuine in-app loading

![Existing local initialization state](api28-after/loading-large.png)

This is the existing “Opening your local table…” state during real local restoration after a display configuration change. It was captured naturally, without an artificial hold or loading timer. The next gameplay frame is captured after the restored Master turn is actually drawn. The optional loading evidence is not an Android 12 system splash.

## Actual Android 12 / API 31

Actual Android 12 system-splash comparison **could not be completed in this workspace**. The API 31 AVD booted and unlocked, and its baseline activity resolved, but screenshot requests stalled and the emulator reported `Failed to find ColorBuffer` / `Failed to bind to texture: invalid color buffer`. Legacy SwiftShader, current SwiftShader with guest Vulkan disabled, and ANGLE/SwiftShader were tried sequentially on the same saved AVD. One earlier concurrent boot was killed by the workspace memory limit. No app uninstall, wipe or app-code workaround was used.

A captured Android **system boot** frame said “Phone is starting…”; it is not an app splash and is deliberately not presented as branding evidence. No fabricated Android 12 screenshot substitutes for the missing platform capture. The SDK 31 resource tests pass for the charcoal native splash, shared inset ball and exact official studio branding image, but **real Android 12+ visual acceptance remains required in Android Studio on a working accelerated emulator/device**.

## Responsive host renders

These are Robolectric native-graphics renders of the production Compose UI, **not emulator screenshots**. They use the same deterministic records and dimensions as the Phase 2.3 gallery. Host captures do not prove system splash, device rendering, actual keyboard behavior or TalkBack order.

| State | Phase 2.3 | Phase 2.4 |
| --- | --- | --- |
| setup | ![Before setup](../../ui-review/richness/after/setup.png) | ![After setup](host-after/setup.png) |
| master-idle | ![Before master-idle](../../ui-review/richness/after/master-idle.png) | ![After master-idle](host-after/master-idle.png) |
| master-after-hit | ![Before master-after-hit](../../ui-review/richness/after/master-after-hit.png) | ![After master-after-hit](host-after/master-after-hit.png) |
| history | ![Before history](../../ui-review/richness/after/history.png) | ![After history](host-after/history.png) |
| history-expanded | ![Before history-expanded](../../ui-review/richness/after/history-expanded.png) | ![After history-expanded](host-after/history-expanded.png) |
| settings | ![Before settings](../../ui-review/richness/after/settings.png) | ![After settings](host-after/settings.png) |
| master-short | ![Before master-short](../../ui-review/richness/after/master-short.png) | ![After master-short](host-after/master-short.png) |
| master-tall | ![Before master-tall](../../ui-review/richness/after/master-tall.png) | ![After master-tall](host-after/master-tall.png) |
| master-landscape | ![Before master-landscape](../../ui-review/richness/after/master-landscape.png) | ![After master-landscape](host-after/master-landscape.png) |
| master-font-130 | ![Before master-font-130](../../ui-review/richness/after/master-font-130.png) | ![After master-font-130](host-after/master-font-130.png) |
| master-font-200 | ![Before master-font-200](../../ui-review/richness/after/master-font-200.png) | ![After master-font-200](host-after/master-font-200.png) |

Host responsive cases cover 360 × 640, 360 × 800, 400 × 900, landscape and 130%/200% fonts. At larger text sizes, the official square mark accompanies a readable separate studio-name label. The red-ball glyph remains the same density-independent vector; it is not re-typeset or replaced by a different ball for each screen.

Physical-device haptics/performance, TalkBack reading order, real keyboard occlusion and the latest Android gesture-navigation/edge-to-edge behavior remain owner/device checks. Platform splash size, mask, branding-image placement and lifetime are controlled by Android; owner visual acceptance is not implied by these screenshots or automated tests.
