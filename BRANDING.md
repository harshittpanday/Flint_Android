# Flint Android branding inventory

This inventory records the inherited visual assets that remain after the Phase 1 textual-branding milestone. It is documentation for a dedicated icon and visual-branding milestone; none of these assets should be replaced without approved Flint artwork.

## Product identity

- Product name: **Flint**
- Full contextual name: **Flint Android**
- Visual direction: near-black or dark charcoal, warm orange or amber, clean and minimal
- Platform direction: the desktop Flint experience adapted for mobile

## Deferred launcher icon

The current launcher icon visibly uses inherited Minecraft/Pojav crafting-table artwork. It remains temporary in all density variants:

- `app_pojavlauncher/src/main/res/mipmap-mdpi/`
- `app_pojavlauncher/src/main/res/mipmap-hdpi/`
- `app_pojavlauncher/src/main/res/mipmap-xhdpi/`
- `app_pojavlauncher/src/main/res/mipmap-xxhdpi/`
- `app_pojavlauncher/src/main/res/mipmap-xxxhdpi/`

Each directory contains some or all of `ic_launcher.png`, `ic_launcher_round.png`, and `ic_launcher_foreground.png`. The adaptive-icon wrappers are:

- `app_pojavlauncher/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app_pojavlauncher/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app_pojavlauncher/src/main/res/values/ic_launcher_background.xml`

### Runtime usage

- `app_pojavlauncher/src/main/AndroidManifest.xml` uses `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round` for the application icon.
- `app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/scoped/FolderProvider.java` uses `R.mipmap.ic_launcher` for storage roots and documents.

### Preview-only usage

- `app_pojavlauncher/src/main/res/layout/item_minecraft_account.xml` references the launcher icon through a `tools:` preview attribute.
- `app_pojavlauncher/src/main/res/layout/view_mod.xml` references the launcher foreground through `tools:` preview attributes.

## Other inherited visual assets to review

- `app_pojavlauncher/src/main/res/drawable/notif_icon.xml` is the notification small icon used by notification, progress, and game services.
- `app_pojavlauncher/src/main/res/drawable/ic_setting_sign_in_background.webp` is displayed by the control-import screen.
- `app_pojavlauncher/src/main/assets/pojavlauncher.png` contains inherited crafting-table artwork.
- `app_pojavlauncher/src/main/assets/pojavtext.png` is a legacy Pojav-named image asset.

The two assets under `src/main/assets` have no direct source-code or resource reference in the current tree, but they remain preserved for attribution and migration review.

## Intentionally retained technical identity

Phase 1 does not change `net.kdt.pojavlaunch`, the `app_pojavlauncher` module and source paths, native library names, Gradle coordinates, historical references, licenses, copyright notices, or upstream dependency credits. These are technical identity or attribution rather than user-facing Flint product copy.
