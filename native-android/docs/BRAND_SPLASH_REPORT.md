# Phase 2.4 — Brand Consistency and Visual Fidelity

Status: implemented and automated checks passed. Actual API 28 review is provided; Android 12+ visual capture is blocked by workspace emulator graphics. Final owner visual approval remains pending.

Branch: `development/native-android-brand-splash`.

Base: `153ffa1539b3480be3e181538b3f5241ac8305b4` on `development/native-android-ui-richness`, preserved unchanged.

Commit: resolve the local commit introducing this report using `git log -1 --format=%H -- native-android/docs/BRAND_SPLASH_REPORT.md`. The final handoff supplies its full SHA. No push or merge is authorized or performed.

## Approved package and asset fidelity

Read both supplied Markdown documents and inspected all three official logos and both visual references before implementation. All seven supplied files match the ZIP bytes. `BRAND_SPLASH_SPEC.md`, original source PNGs and references retain their supplied paths. The root handoff is preserved at `docs/PHASE_2_4_CODEX_HANDOFF.md` to keep all repository changes under `native-android/`.

The official blue-and-white ND artwork is used directly. No arrow, lettering or monogram was redrawn or generated. The runtime horizontal lockup is a lossless crop of the official dark horizontal logo: source rectangle `(112, 128, 1144, 336)`, resulting in 1032 × 208 pixels. Pixel comparison confirms exact equality with that source rectangle. The compact icon is a byte-identical copy of the supplied square logo. The originals remain unmodified. See [asset provenance and hashes](brand/README.md).

The uploaded logos have opaque dark backgrounds. Those backgrounds are preserved, with the logo rendered on a neutral dark header. No speculative transparency removal, recoloring, sharpening or invented LLC lettering was added. The compact fallback uses the official square image plus a separate studio-name label. Meaningful studio branding is announced once; the game icon is decorative beside the already spoken app title.

The supplied shield/three-ball concepts are illustrations rather than clean production emblems. This implementation selects the existing native crisp red 3-ball vector as the single master. Its ball, number patch and number paths/colors are unchanged; only the old opaque square background path is removed. The previous separately drawn Compose ball is replaced by that drawable. A clean approved shield asset remains a future design input.

## Implemented files

| File | Change |
| --- | --- |
| `app/src/main/java/com/nodetoursstudio/threeball/ui/DesignSystem.kt` | Shared vector game mark, official horizontal studio lockup, responsive compact official-icon/name fallback, compact dark-neutral header. Premium canvas, hero, buttons, standings and screen organization remain unchanged. |
| `app/src/main/res/drawable/ic_three_ball.xml` | Remove opaque square; keep existing ball artwork. Header, startup and launcher reference this master. |
| `res/drawable/startup_background.xml`, `res/values/themes.xml` | API 26–30 dark starting window with centered vector. No full-screen bitmap or timed splash. |
| `res/values-v31/themes.xml`, `res/drawable/brand_splash_icon.xml` | Native Android 12+ charcoal system splash, safely inset shared ball, matching icon background, real official studio crop in platform branding-image slot, dark system bars. Platform controls transition timing and masking. |
| `res/mipmap-anydpi/ic_launcher.xml`, `res/drawable/launcher_foreground.xml`, manifest icon attribute | Adaptive launcher with charcoal background and safely inset shared vector foreground. Backup policy, application ID, exported activity and permissions are unchanged. |
| `res/drawable-nodpi/studio_header.png`, `studio_compact.png` | Exact source-preserving runtime studio assets. Aspect ratio is maintained; nodpi avoids additional density-bucket scaling. Combined decoded memory is approximately 1 MiB. Original/reference full-resolution images are documentation, not APK backgrounds. |
| `res/values/colors.xml`, `res/values/strings.xml` | Shared startup canvas color and accessible studio brand name; replace unused former tiny plain-text studio label. |
| `app/src/test/.../BrandStartupTest.kt` | Transparent master corners, visible centered legacy startup, adaptive launcher, API 31 native splash/background/official branding resource checks. |
| `docs/BRAND_SPLASH_SPEC.md`, `docs/PHASE_2_4_CODEX_HANDOFF.md`, `docs/brand/`, README links | Unchanged approved package, asset provenance, actual emulator comparisons and responsive host evidence. |

Resource paths abbreviated as `res/` are under `app/src/main/res/`. Shared master wrappers do not duplicate the ball artwork. No new dependencies, services, accounts or cloud integration were added.

## Startup and gameplay safeguards

No artificial startup timer, keep-on-screen predicate, delayed pre-draw, sleep or replacement full-screen artwork was introduced. The existing initialization state still shows its opening message and progress only while local loading is genuinely needed, and its existing Retry/error handling remains intact. No new loading flow was necessary. Android controls the native splash; hot resumes may omit it.

Scoring engine, records, ViewModel transitions, repositories, Room migrations, DataStore, flags, JSON schemas/contracts, web and Capacitor code are unchanged. The Activity and accepted-action haptic logic are unchanged. All modifications are under `native-android/`.

The existing API 28 application was updated in place with `pm install -r -t`; no uninstall or data clear was used. After cold launch, restoration and navigation through History/Settings, the exact serialized active Novice game and all three completed records matched the pre-update snapshot. Their sorted-payload SHA-256 was `d0b0b7fe350c5a4e65a5eaa7cd609ea30fe07f0920359243d83f9e4e5ae98e75` before and after. Database copies remain local, outside committed artifacts. The final-APK walkthrough then completed/archived that existing sample Novice through the normal UI and started a new Master sample. The original three completed payloads are still byte-identical, and the former active game is archived under its original game ID. Final state has four completed games and one active Master turn at −2. Its active/history payload snapshot remained identical through cold restarts, resizing and font-scale review: SHA-256 `dafa0fb762f21908d998edb7ea762e091ad38eec29891ab4c7174b4aa7ad9c92`. No existing result was overwritten or discarded.

## Verification

Commands use the native wrapper with JDK 17 and the local Android SDK:

```
./gradlew :engine:test :records:test :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-daemon
node tools/verify-web-reference.mjs
python3 tools/verify-contract.py
```

Run from `native-android/`. A clean app rebuild was also used after moving the resource directory to avoid stale incremental resource state.

| Check | Result |
| --- | --- |
| Engine unit tests | 28 passed |
| Records/serialization tests | 12 passed |
| App JVM/Robolectric tests | 64 passed |
| Total | **104 passed; zero failures, errors or skipped tests** |
| JavaScript parity | **16 original-handler scenarios passed** |
| JSON compatibility | **3 portable fixtures valid**, future versions and synchronization eligibility rejected |
| Debug assembly | **BUILD SUCCESSFUL**; debug APK remains local and untracked |
| Lint | **Passed: zero errors, one warning** (`MonochromeLauncherIcon`, documented below) |
| Supplied asset preservation | **7 of 7 files byte-identical** to ZIP |
| Runtime studio assets | **Exact source crop pixels and byte-identical compact image verified** |
| Existing active game/history after update | **Exact serialized payloads unchanged** |

All original 100 tests remain. Four new branding/startup tests cover transparent framing, legacy startup centering, adaptive launcher and Android 12 platform configuration. APKs, SDK/system images, generated test/lint outputs, database copies and temporary capture scripts are not committed. Final local debug APK SHA-256: `392d787024df44f4f661d76fe0e8e7daba7119c565677ed96e82dea3880f0b04`.

## Visual review and differences

See [the comparison gallery](brand/review/README.md) for actual emulator screencaps, baseline provenance and responsive Robolectric host renders. Earlier phase documentation and screenshots are retained unchanged. The gallery includes **19 actual API 28 screencaps** (8 baseline, 11 Phase 2.4) and **11 new host renders**. Five baseline game/setup frames are copied unchanged from the approved Phase 2.3 gallery; remaining baseline frames were captured before updating. The legacy startup frame was captured in the initial Phase 2.4 build with identical API 28 appearance; gameplay/Setup/History/Settings and responsive captures were refreshed with the final APK.

Actual captures cover default/Master Setup, Novice, Master at zero/−2, History, Settings, the real existing local-restoration loading state, a 420 × 900 logical display override and 150% fonts. The override is rendered into a raw 360 × 800 physical screencap by the emulator; it is not labelled a native 420-pixel screenshot. Host cases additionally cover short phones, 400 × 900, landscape and 130%/200% fonts. Routine Master controls fit the normal 360 × 800 capture; large-font content scrolls without shrinking controls.

The first responsive screenshots caught starting/blank windows during recreation. Inspection rejected those as gameplay evidence; final captures waited until both official header and restored player hero were actually drawn. A naturally captured “Opening your local table…” frame is separately labelled genuine loading, not a system splash or gameplay screen. All screencaps are unedited.

- Preserve the approved compact native Play / History / Settings and Scorecard structure; concept art's oversized headers, alternative tabs, invented statistics and shield composition are not implemented.
- Official studio images retain their actual dark background rather than pretending they were transparent. Header uses the supplied lettering and blue/white mark, with a compact official-icon/name variant for narrow layouts and large fonts.
- The existing native red-ball vector is selected for startup/header consistency. It is simpler than the former independently drawn glossy Compose ball and is not a substitute shield. A production shield and any future monochrome launcher artwork require separate approval.
- Android controls final system splash placement, mask, size, branding-image visibility and lifetime. No complete concept image is substituted for the system splash. The legacy startup has the game mark; Android 12+ can also show the official studio image in its supported branding slot.
- The optional Android 13+ themed monochrome launcher icon is intentionally absent. Lint's `MonochromeLauncherIcon` warning is documented rather than inventing a replacement emblem or supplying a misleading solid-circle mask. Full-color adaptive launcher and native system splash are implemented.
- Physical-device tactile quality, TalkBack reading order and the latest Android gesture-navigation/edge-to-edge behavior still need owner/device acceptance. The known Phase 2.3 gameplay layout is preserved, and responsive host renders supplement actual emulator coverage.
- Concurrent software emulators and Gradle exceeded the workspace memory limit and killed the first API 31 boot. Verification was then scheduled sequentially. A later API 31 boot completed but the legacy `swiftshader_indirect` renderer reported invalid color buffers and screenshot timeouts; the saved AVD was retried with current SwiftShader (guest Vulkan disabled) and ANGLE/SwiftShader. Those also failed to provide reliable app screenshots with invalid color-buffer errors, despite successful boot/unlock and activity resolution. **Actual Android 12+ splash/header visual verification could not be completed here and remains an explicit Android Studio/device acceptance gap.** No Android system-boot frame or host preview is represented as an app system-splash capture. The API 28 cold restart also briefly showed a System UI ANR; Wait was used before the final walkthrough, as in the earlier phase. The existing API 28 app data was retained and checked; no wipe/uninstall workaround was used. This is a test-environment limitation, not a scoring/storage change.

## Android Studio acceptance still required

Use a working hardware-accelerated Android 12+ emulator/device to review a cold launch from the launcher icon, native splash masking/charcoal background and official studio branding slot. Check that the existing opening/error state ends immediately on readiness and a returning game restores. Review Setup, dense Master scoring, History and Settings at normal/large fonts, then modern gesture insets and TalkBack order. Preserve the installed app data and use a compatible signing key for in-place testing; no data reset is part of this handoff.

Final visual approval remains with the owner after Android Studio review.
