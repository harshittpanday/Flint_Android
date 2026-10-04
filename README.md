# Flint Android

> Minecraft Java Edition on Android — the Flint way.

Flint Android is the Android edition of the **Flint Launcher ecosystem**, focused on bringing Minecraft: Java Edition to Android with a clean, simple, and Flint-style experience.

> [!IMPORTANT]
> Flint Android is currently in **early development**.
> The project can be built as an Android APK, but Flint-specific features and UI are still being developed.

## Current Status

- [x] Android project initialized
- [x] Baseline APK builds successfully
- [ ] Flint UI/UX
- [ ] Flint profiles
- [ ] Minecraft launch testing on real Android hardware
- [ ] Touch controls
- [ ] Keyboard & mouse support
- [ ] Flint Client integration
- [ ] AutoAuth integration
- [ ] Public Android release

## Goals

Flint Android aims to keep the same philosophy as the desktop launcher:

- Simple **Profile → Play** experience
- Clean Flint UI instead of complicated launcher screens
- Minecraft Java Edition on Android
- Mobile-friendly controls
- Keyboard & mouse support for tablets, Android PCs and similar devices
- Flint Client integration
- Performance suitable for lower-end hardware
- Open-source development

## Flint Ecosystem

Flint is being developed across multiple platforms and components:

- **Flint** — Windows launcher
- **Flint Linux** — Linux edition
- **Flint Android** — Android edition
- **Flint Client** — in-game Fabric client
- **Flint Website** — website and downloads
- **Flint DC Bot** — community/Discord tooling

## Building

Flint Android currently uses the Android/Gradle build system.

### Requirements

- Git
- JDK 21
- JDK 8
- Android SDK Platform 34
- Android Build Tools 34
- Android NDK `25.2.9519653`

Clone the repository:

```bash
git clone https://github.com/harshittpanday/Flint_Android.git
cd Flint_Android