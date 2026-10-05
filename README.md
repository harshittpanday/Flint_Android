# Flint Android

> Minecraft: Java Edition on Android — the Flint way.

Flint Android is the Android edition of the Flint launcher ecosystem. It is focused on a clean, simple, mobile-friendly **Profile → Play** experience for Minecraft: Java Edition.

> [!IMPORTANT]
> Flint Android is in early development. The project builds as an Android APK, but Flint-specific features and the full Flint interface are still being developed.

## Project lineage

Flint Android is derived from PojavLauncher and builds on its Android Minecraft: Java Edition platform work.

The inherited PojavLauncher code, platform work, copyright notices, dependency credits, and project history remain attributed to their original authors and contributors. Flint does not claim authorship of that inherited work. See [LICENSE](LICENSE) and the notices retained throughout the source tree for licensing details.

Phase 1 changes only safe, user-facing product text. The existing PojavLauncher application ID, namespace, package names, source layout, and temporary artwork remain in place until dedicated migration milestones.

## Current status

- [x] Android project initialized
- [x] Baseline APK builds successfully
- [x] Phase 1 Flint product identity
- [ ] Flint UI/UX
- [ ] Flint profiles
- [ ] Dedicated Flint Android icon
- [ ] Minecraft launch testing on real Android hardware
- [ ] Touch controls
- [ ] Keyboard and mouse support
- [ ] Flint Client integration
- [ ] AutoAuth integration
- [ ] Public Android release

## Direction

Flint Android will use Flint's established visual identity:

- Near-black and dark-charcoal surfaces
- Warm orange and amber accents
- A clean, minimal interface
- Minecraft-inspired character without depending on Minecraft artwork
- A mobile adaptation of the desktop Flint launcher rather than a recolored upstream interface

## Flint ecosystem

- **Flint** — Windows launcher
- **Flint Linux** — Linux edition
- **Flint Android** — Android edition
- **Flint Client** — in-game Fabric client
- **Flint Website** — website and downloads
- **Flint DC Bot** — community and Discord tooling

## Building

### Requirements

- Git
- JDK 21
- JDK 8
- Android SDK Platform 34
- Android Build Tools 34
- Android NDK `25.2.9519653`

Clone and build a debug APK:

```bash
git clone https://github.com/harshittpanday/Flint_Android.git
cd Flint_Android
./gradlew :app_pojavlauncher:assembleDebug
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Deferred branding assets

The inherited launcher icon and other legacy visual assets are intentionally unchanged because final Flint Android artwork is not yet available. See [BRANDING.md](BRANDING.md) for the replacement inventory and current usage locations.
