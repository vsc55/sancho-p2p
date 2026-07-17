# Changelog

Notable changes to this preservation/modernization of **Sancho**.
Format loosely based on [Keep a Changelog](https://keepachangelog.com/).

The upstream project's original changelog (2004–2006) is preserved at
[`appimage/usr/bin/distrib/ChangeLog`](appimage/usr/bin/distrib/ChangeLog). The
authentic early **0.9.4-23** source lives at the `0.9.4-23` tag
(`git checkout 0.9.4-23`).

## [Unreleased]

### Build & tooling

- CI: the **Build** check is now a single lightweight Linux compile (the full
  multi-OS packaging runs in **Release**) and skips `Release …` commits, so a
  release push no longer runs two overlapping workflows at once.

## [0.9.4-62] — 2026-07-17

### Added

- **Cross-platform build (Linux / macOS / Windows).** The pom now auto-selects the
  stock SWT fragment for the build machine via OS/arch profiles
  (`win32`, `gtk.linux.x86_64/aarch64`, `cocoa.macosx.x86_64/aarch64`); override
  with `-Dswt.artifactId=…`. The CI **Build** workflow runs on a
  Linux/Windows/macOS matrix.
- **Cross-platform release artifacts.** The release workflow is now a
  Windows/Linux/macOS matrix that publishes, in one Release: the Windows portable
  `.zip` + jar, Linux `.deb` + `.rpm`, and a macOS `.dmg`. `build-exe.ps1` became
  the cross-platform `tools/build-app.ps1 -Type <app-image|deb|rpm|dmg>`.

### Changed

- Only **JFace** is now signature-stripped (it is platform-independent); SWT is
  used stock/signed per platform, decoupled from the unsigned-JFace artifact.
  `tools/unsign-libs.ps1` and `local-repo/` updated accordingly.

## [0.9.4-61] — 2026-07-17

### Changed

- Replaced the abandoned **`com.jcraft:jsch:0.1.55`** with the maintained fork
  **`com.github.mwiede:jsch:0.2.23`** (same `com.jcraft.jsch` package, so a
  drop-in), which carries security fixes and modern algorithms.

### Build & tooling

- Added a **Build** workflow ([`.github/workflows/build.yml`](.github/workflows/build.yml))
  that compiles and packages on every push and pull request to `main`, so
  regressions surface before a release tag is cut. Added build and release
  badges to the README.

## [0.9.4-60] — 2026-07-17

First versioned release of the modernized build. `sancho-src/src` is the
**0.9.4-59** source recovered by decompiling the last published binary (the
authentic source was lost), ported to build and run on a current JDK, plus bug
fixes found in review.

### Build & tooling

- **Maven build** (`pom.xml`) targeting **SWT 3.124 / JFace 3.31 on JDK 17+**;
  produces `sancho-0.9.4-60.jar`.
- **VS Code** config ([`.vscode/`](.vscode)) with a ready "Sancho (GUI)" run
  configuration (F5) including the required VM args.
- **Windows portable executable** via `jpackage`
  (`tools/build-exe.ps1`, later renamed): `sancho.exe` with a bundled Java
  runtime. The launcher is named/started so the app's built-in
  *Preferences → Windows registry* page wires `.torrent` / `ed2k:` double-click to
  the core.
- **GitHub Actions release** ([`.github/workflows/release.yml`](.github/workflows/release.yml)):
  tag a commit → build the jar + exe → publish a GitHub Release.
- Signature-stripped SWT/JFace in a project-local Maven repo
  ([`local-repo/`](local-repo)) so Sancho's vendored `org.eclipse.jface.viewers`
  classes load into the (otherwise signed) package; regenerate with
  [`tools/unsign-libs.ps1`](tools/unsign-libs.ps1).

### Source recovery & port (from 0.9.4-59)

- Decompiled with **Vineflower** (flat classes, synthetics kept, generics off) so
  the output recompiles.
- Ported the 2008-era code to modern SWT/JFace: stubbed the GCJ-only `Prov`
  bootstrap; used reflection for `MenuManager` / `FieldEditorPreferencePage`
  private members; delegated the custom viewers' `preservingSelection` to the
  modern base; UTF-8 sources; added casts where raw collections lost their element
  types; 32→64-bit COM handles.

### Fixed

- **Win32 web browser tab** was broken on 64-bit SWT — its `COMObject` callbacks
  were declared `int method(int[])` and no longer override modern SWT's
  `long method(long[])`. The web tab now uses the standard `Browser` widget, which
  supports win32 natively. Removed the dead `WebBrowserTab_win32` /
  `LinkEntryItem_win32` code.
- **Browser engine**: replaced the removed `SWT.MOZILLA` style with **`SWT.EDGE`**
  (Chromium/WebView2) on Windows.
- **Table sorting crash**: the sorters compare live model data that the core
  mutates during a sort, so modern Java's TimSort throws *"Comparison method
  violates its general contract"*. Requested the legacy merge sort in `main()` so a
  plain `java -jar` survives too (not only the exe / VS Code launch config).
- **Preference icons**: restored the preference-tree node icons (modern JFace
  dropped the `PreferenceNode(id, page, imageDescriptor)` constructor) via a small
  `ImagePreferenceNode`.

### Known limitations

- This is **decompiled code**, not the authentic 0.9.4-59 source (lost): no
  original comments or local-variable names.
- The GCJ-only `Prov` provider bootstrap is a no-op stub (irrelevant on a stock
  JVM).
- The SWT Mozilla/XULRunner backend is gone; the legacy `forceMozilla` preference
  now maps to Edge on Windows.
- Cell editing and preference icons are implemented per modern JFace behavior but
  were only verified to build and launch, not click-tested in a live session.
