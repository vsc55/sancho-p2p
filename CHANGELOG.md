# Changelog

Notable changes to this preservation/modernization of **Sancho**.
Format loosely based on [Keep a Changelog](https://keepachangelog.com/).

The upstream project's original changelog (2004–2006) is preserved at
[`appimage/usr/bin/distrib/ChangeLog`](appimage/usr/bin/distrib/ChangeLog). The
authentic early **0.9.4-23** source lives at the `0.9.4-23` tag
(`git checkout 0.9.4-23`).

## [0.9.4-69] — 2026-07-18

### Fixed

- **Navigation toolbar invisible at startup.** The main window laid out only the
  inner CoolBar composite, so the outer `GridLayout` gave the toolbar row no height
  until the first manual resize (the button bar under the menu was intermittently
  missing on launch). It now forces a full `shell.layout(true, true)` once the
  window is open at its restored size.
- **Displayed version stuck at `0.9.4-59`.** `VersionInfo` had the number baked in
  from the decompiled source; it now reads the real build version, filtered in from
  `${project.version}` via a `version.properties` resource (falls back to
  `0.9.4-dev` when run from unbuilt sources).

### Changed

- **Embedded web browser now uses Edge on Windows.** With the old Mozilla/XULRunner
  backend gone, the browser defaulted to the legacy Internet Explorer engine
  (`SWT.NONE`) on Windows. It now creates the modern Edge/Chromium (WebView2)
  browser by default, falling back to the platform default if WebView2 is
  unavailable. The obsolete **"Force mozilla"** preference and its wiring were
  removed.

## [0.9.4-68] — 2026-07-18

### Changed

- **Cleaner build output.** The uber-jar no longer emits maven-shade
  "overlapping resource" warnings — the duplicate Eclipse/OSGi build metadata
  (`.options`, `.api_description`, `about.html`, `about_files/**`,
  `plugin.properties`) and the per-jar `MANIFEST.MF` are excluded, and the
  manifest is regenerated with the `Main-Class`. The vendored JFace in
  `local-repo/` now ships `.sha1`/`.md5` checksums (and `unsign-libs.ps1`
  writes them) so Maven stops warning about missing checksums. Only the upstream
  SWT `${osgi.platform}` POM warning (from Eclipse's own artifact) remains.

### Fixed

- **Release runs are serialized** with a `concurrency` group, so moving a tag to
  re-release cancels the in-flight run instead of leaving two runs deleting,
  recreating and uploading to the same GitHub Release at once.

### Docs

- README: dropped the stale **GNU RegExp** dependency mention (it was removed in
  0.9.4-64).

## [0.9.4-67] — 2026-07-18

### Added

- **Windows `.msi` installer.** Releases now ship `sancho-<ver>-win64.msi` (WiX 3
  via jpackage) next to the portable zip: it installs to Program Files with
  Start-menu / desktop shortcuts and a stable upgrade code (new versions replace
  old ones). The setup dialog has an opt-out checkbox to register the `.torrent`
  file type and the `ed2k` / `magnet` / `sig2dat` URL protocols under
  `HKLM\Software\Classes` (command `"…\sancho.exe" "-l" "%1"`, matching the app's
  own Windows-registry page). Silent installs control it with the public
  `REGISTERASSOC` property (`msiexec /i … /qn REGISTERASSOC=0`).

## [0.9.4-66] — 2026-07-18

### Fixed

- **macOS external links**: `WebLauncher` keyed the native `/usr/bin/open` on the
  removed `carbon` SWT platform, so modern macOS (`cocoa`) fell through to the
  generic path. It now handles `cocoa` too.

### Changed

- Platform-code audit: removed dead checks for long-gone SWT platforms
  (`motif` in the tray icon; `carbon` for the sort-indicator default) and skip the
  extra table mouse listener on `cocoa` as well (matching the old `carbon` intent).

### Build & tooling

- Added **Dependabot** (`.github/dependabot.yml`): weekly updates for GitHub
  Actions and Maven dependencies/plugins, ignoring the compatibility-pinned ones
  (Eclipse SWT — coupled to the vendored unsigned JFace — and Trove major).

## [0.9.4-65] — 2026-07-18

### Build & tooling

- CI: the Release workflow no longer uses `actions/upload-artifact` /
  `download-artifact` (which still forced the deprecated Node 20). A
  `prepare-release` job creates the Release, then each platform job uploads its
  packages straight into it with `gh release upload`. No more Node 20 warnings.

## [0.9.4-64] — 2026-07-18

### Changed

- Dropped the abandoned **`gnu-regexp:1.1.4`** (~2001) dependency. Regexes now run
  on the JDK's `java.util.regex`, behind a thin `sancho.utility.regex` adapter
  (`RE` / `REMatch` / `REException`) so the ~24 call sites only needed their
  imports changed. All patterns — including the `ed2k` / `magnet` / `.torrent`
  link parsing — were verified to compile and match under `java.util.regex`.

### Build & tooling

- CI: bump `actions/upload-artifact` and `actions/download-artifact` to v5
  (Node 24) to clear the Node 20 deprecation warning in the Release workflow.

## [0.9.4-63] — 2026-07-18

### Added

- **Self-contained runnable uber-jar.** `mvn package` also produces
  `sancho-<version>-all.jar` (maven-shade-plugin, jar signatures and duplicate
  `module-info` stripped) that bundles every dependency, so it runs with just
  `java -jar`. Releases now ship a per-platform uber-jar
  (`sancho-<ver>-<platform>.jar`) alongside the native packages, replacing the
  old classpath-only thin jar.

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
