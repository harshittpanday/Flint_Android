# Flint Android branding inventory

This document records the visual assets adopted during the Phase 2 branding milestone and their provenance.

## Canonical source

The artwork is reused from the Windows Flint repository without modifying that repository:

- `branding/flint-logo-source.png` is the documented canonical pixel-art logo source.
- `src-tauri/icons/android/` provides the checked-in Android launcher, round, foreground, adaptive-icon, and background resources.
- `src-tauri/icons/icon.svg` provides the existing Flint `F` vector used to derive the monochrome Android notification icon.

No new logo or visual identity was invented for Flint Android.

## Android launcher icon

The canonical density-specific Android resources are installed under:

- `app_pojavlauncher/src/main/res/mipmap-mdpi/`
- `app_pojavlauncher/src/main/res/mipmap-hdpi/`
- `app_pojavlauncher/src/main/res/mipmap-xhdpi/`
- `app_pojavlauncher/src/main/res/mipmap-xxhdpi/`
- `app_pojavlauncher/src/main/res/mipmap-xxxhdpi/`

Each density contains `ic_launcher.png`, `ic_launcher_round.png`, and `ic_launcher_foreground.png`. Adaptive-icon resources are:

- `app_pojavlauncher/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app_pojavlauncher/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app_pojavlauncher/src/main/res/values/ic_launcher_background.xml`

The existing manifest and scoped-storage provider continue to reference `ic_launcher` and `ic_launcher_round`, so no runtime code or application identity changes were required.

## Notification icon

`app_pojavlauncher/src/main/res/drawable/notif_icon.xml` uses the exact `F` path from Flint's existing `src-tauri/icons/icon.svg`, rendered as the monochrome white silhouette required for an Android small notification icon. It is used by the notification, progress, and game services through their existing resource reference.

## Removed legacy visuals

The unreferenced inherited assets `app_pojavlauncher/src/main/assets/pojavlauncher.png` and `app_pojavlauncher/src/main/assets/pojavtext.png` were removed. The prior launcher and notification artwork was replaced by the canonical Flint resources above.

## Intentionally unchanged

- `app_pojavlauncher/src/main/res/drawable/ic_setting_sign_in_background.webp` remains unchanged. It is Minecraft landscape artwork used by the control-import screen, not Pojav product branding; replacing it belongs to a later UI redesign.
- Application ID, namespace, Java packages, module and source paths, native names, storage paths, URLs, protocols, authentication, renderer, controls, runtime management, and launcher behavior remain unchanged.
- PojavLauncher lineage, licenses, copyright notices, upstream links, translator credits, dependency credits, and historical attribution remain preserved.
