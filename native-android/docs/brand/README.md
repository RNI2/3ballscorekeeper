# Official branding assets

The three files in `source/` and both concepts in `references/` are unmodified uploads. The concepts are visual references, not official replacement artwork. No invented monogram, arrow redraw, generated logo, or shield crop is used by the app.

## Packaged studio artwork

- `app/src/main/res/drawable-nodpi/studio_header.png`: lossless crop of the official dark horizontal source, rectangle `(112, 128, 1144, 336)` in original pixels (left/top inclusive, right/bottom exclusive). Result: 1032 × 208. No resizing, recoloring, sharpening, alpha removal or lettering changes. Every resulting pixel matches the source rectangle exactly.
- `app/src/main/res/drawable-nodpi/studio_compact.png`: byte-identical copy of the official square icon. The compact header pairs it with a separate accessible studio-name label; that label does not replace official artwork.
- The originals have opaque dark backgrounds. The header retains those backgrounds and uses a dark neutral parent rather than attempting an unapproved transparency conversion. Runtime Compose rendering preserves aspect ratio and scales with display density; `nodpi` prevents redundant resource-density resampling. Combined decoded studio bitmap storage is approximately 1 MiB, with no full-screen bitmap background.

## Shared game identity

`drawable/ic_three_ball.xml` is the pre-existing native launcher vector with only its opaque square background path removed. The red ball, cream number patch and number path are unchanged. Header, legacy startup background, Android 12+ splash inset and adaptive launcher foreground all reference this single master. Vector resources are density independent. The old independently drawn Compose ball is removed, so startup/header no longer disagree.

This is the existing crisp red 3-ball identity, not the complex shield concept. A clean, separately approved shield/emblem production asset remains a future design input. No replacement was invented.

The launcher foreground has a 16.67% inset; the system splash wrapper has a 12.5% inset to keep the mark inside the platform safe region. Wrappers reuse the vector rather than copying its paths. Charcoal icon background, starting window, status bar and navigation bar colors coordinate with the existing UI. The Android 12+ platform branding-image slot also references the same official horizontal studio crop; it is not baked into the system icon. Android controls the final icon mask, branding slot, size and splash duration.

## Supplied source hashes

| File | SHA-256 |
| --- | --- |
| `references/3_ball_scorekeeper_play_screen.png` | `aec408881748ca67de79d805cb63548c2fac8a3c73702293d9316a8e44e95257` |
| `references/emerald_3_ball_scorekeeper_splash_screen.png` | `771cf0065c78090b70dc2bfd6932aba6df65bf5184f5aea3afa806055d962dc9` |
| `source/No_Detours_Studio_Dark_Horizontal.png` | `a45cfe0a5d374c2afbe00589d20890c485d6e6f53bfc9dc18d33f0db76ff47a9` |
| `source/No_Detours_Studio_Primary_Logo.png` | `866c1025adfcfcc307f31b3191e76bca7a2e21a8707bd57f1b2ba18bc2168482` |
| `source/No_Detours_Studio_Square_Icon.png` | `48b68567a848dc8dac8ea853e17f5e94e2bafd3604f0d434357d0e12effde770` |
