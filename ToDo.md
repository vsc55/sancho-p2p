# ToDo — potential improvements

Backlog of improvements for the modernized `sancho-p2p` build. Done items live in
[CHANGELOG.md](CHANGELOG.md).

## High value

- [x] ~~Cross-platform **build**~~ — done: OS/arch pom profiles auto-select the SWT
  fragment, JFace decoupled/unsigned, CI builds on Linux/Windows/macOS. See
  CHANGELOG.
- [x] ~~Cross-platform **release artifacts**~~ — done: the release workflow is a
  Windows/Linux/macOS matrix publishing Windows `.zip` + jar, Linux `.deb` +
  `.rpm`, and macOS `.dmg`. See CHANGELOG.

- [x] ~~WebBrowser: restore or retire the Ctrl+click "grab page as .torrent"
  feature~~ — done: **retired**. `ctrlDown()` was hard-wired to `false` and
  `setupCtrlKey()` was an empty no-caller stub; SWT doesn't reliably deliver key events
  over a native browser control, so the feature can't be resurrected as designed.
  Removed the dead methods and the unreachable `WebBrowserTab$6` branch. See CHANGELOG.
- [x] ~~WebBrowser: decode dropped-URL bytes with an explicit charset~~ — done:
  `UniformResourceLocator.nativeToJava` now decodes the Windows shell
  `UniformResourceLocator` format as `windows-1252` and the Gecko `text/x-moz-url-data`
  format as UTF-16LE (the latter was also being truncated to one char by scan-to-NUL),
  instead of the JDK-18+ default UTF-8. See CHANGELOG. Verify with a non-ASCII URL drop.
- [x] ~~WebBrowser: per-tab address combo~~ — done: each tab's address combo is
  bound to its `CTabItem` and every read/write resolves the selected/relevant tab's
  combo, so with multiple tabs the URL no longer lands in the last-created tab's bar.
  See CHANGELOG.
- [ ] **Verify / fix chunk-bar blur on HiDPI (>100% display scaling).** The Downloads
  "chunks" column and the chunk detail dialogs round-trip through
  `ImageData.scaledTo()` (`ChunkImageData` / `ChunkCanvas`), which loses device
  resolution on a scaled monitor (125/150/200%) → soft/blurry bars. Not visible at
  100%. Fix candidates: blit the composed bar with the 8-arg scaled `drawImage`
  instead of pre-scaling via `ImageData`, or at least `gc.setInterpolation(SWT.HIGH)`
  + `setAntialias(SWT.ON)` before the blit. Needs a >100% Windows display to observe.

- [x] ~~Verify UTF-8 decoding of core strings (charset fix)~~ — done: confirmed against
  a real MLDonkey core on Windows. `MessageBuffer.getString` decodes core strings as
  explicit UTF-8, and accented download filenames (e.g. "Hospital de campaña",
  "Hermana.pequeña.de.alguien") render correctly with no mojibake.

- [~] **Functional end-to-end verification.** ✅ Verified working on **Windows**
  against a real MLDonkey core. Remaining: validate on **Linux** and **macOS**, and
  spot-check the ported edge flows (sorting live tables, IRC, web browser, cell
  editing, preference icons) on each platform.

## Nice to have

- [x] ~~Uber-jar (shaded)~~ — done: `mvn package` produces `sancho-<ver>-all.jar`
  (maven-shade-plugin); releases ship a per-platform `sancho-<ver>-<plat>.jar` for
  one-file `java -jar`. See CHANGELOG.

- [x] ~~Drop `gnu.regexp`~~ — done: the abandoned `gnu-regexp:1.1.4` dependency is
  gone; regexes run on `java.util.regex` behind a thin `sancho.utility.regex`
  adapter. See CHANGELOG.
- [x] ~~Pure `java.util.regex` migration~~ — done: the 13 call sites were rewritten
  to use `Pattern`/`Matcher`/`PatternSyntaxException` directly and the
  `sancho.utility.regex` adapter (`RE`/`REMatch`/`REException`) was deleted. Mapping
  was 1:1 (`getMatch`→`find`, `getAllMatches`→`while(find())`, `toString(i)`→
  `group(i)`, `getStartIndex/EndIndex(i)`→`start/end(i)`, `substituteAll`→
  `replaceAll`, `REG_ICASE`→`CASE_INSENSITIVE`); exactly-one-match and null-input
  semantics preserved. See CHANGELOG.

- [x] ~~MSI installer~~ — done: releases ship `sancho-<ver>-win64.msi` (jpackage +
  WiX 3) with shortcuts, upgrade code, and an opt-out checkbox / `REGISTERASSOC`
  property to register the .torrent + ed2k/magnet/sig2dat associations. See
  CHANGELOG.
- [x] **Multi-language MSI (single installer, auto-selected by OS language).** Route B,
  done: `tools/build-app.ps1 -Type msi` now emits ONE `sancho.msi` carrying every UI
  language. jpackage builds the base `en` MSI (JDK 21 + WiX 3) and, via `--temp`, leaves
  its WiX intermediates behind; `tools/wix-multilang.ps1` re-lights them once per culture,
  `torch`es each against the base into a language transform (`.mst`), and embeds the
  transforms as LCID-named sub-storages + sets the package language list. The embed uses a
  bundled `tools/wix-embed-transforms.vbs` (WindowsInstaller COM) instead of the Windows
  SDK's `wisubstg.vbs`/`WiLangId.vbs`, so no SDK scripts are needed on the build/CI host.
  Languages: en (1033) + de/ja/zh (1031/1041/2052, free from jpackage's bundled
  `MsiInstallerStrings_*.wxl`) + es (3082), fr (1036), it (1040), pt (2070 Portugal +
  1046 Brazil, one European-Portuguese translation serving both) — the extra ones
  hand-authored in `packaging/windows/wix-lang/MsiInstallerStrings_*.wxl` (standard WiX
  dialog buttons still come free from WixUIExtension's per-culture strings).
  The association checkbox in `ShortcutPromptDlg.wxs` is now a `!(loc.SanchoRegisterAssociationsLabel)`
  token, translated in each language's `.wxl`. To add a language: drop a
  `MsiInstallerStrings_<culture>.wxl` in `packaging/windows/wix-lang/` and add its row to
  the `$EXTRA` table in `wix-multilang.ps1`. NOTE: the extra-language `.wxl` live in
  `wix-lang/` (NOT in jpackage's `--resource-dir`) on purpose — jpackage auto-discovers
  every `MsiInstallerStrings_*.wxl` under `--resource-dir` and, on finding a locale it does
  not bundle (es), makes it the primary culture and mis-builds; only the base `en` override
  (needed so jpackage's own build resolves the checkbox token) stays in `packaging/windows/wix/`.
  Toolchain is now **JDK 25 + WiX 6** (see Option B below), and the per-language step replays
  jpackage's own `wix build` instead of the removed WiX 3 `light`/`torch`.
  Possible follow-up: cover more regional LCIDs that today fall back to English
  (es-MX 2058 and es traditional-sort 1034; fr-CA 3084; de-AT/CH) — cheap, just add
  the LCID to the language's `Lcids` in `wix-multilang.ps1`.
- [x] **(Option B) Modernize the Windows installer toolchain to WiX 6 + jpackage JDK 25.**
  Done: the custom sources are WiX 4+ syntax now (`<Product>`+`<Package>` merged, Component
  `<Condition>` child → `Condition` attribute, implicit `Guid`, `BinaryRef`, `<Custom>`
  conditions as attributes), rebased on the jpackage 25 templates; the placeholder
  `overrides.wxi` was dropped in favour of jpackage's. The `.wxl` moved to the `v4/wxl`
  schema and jpackage 25's renamed/added strings (`InstallDirNotEmptyDlgInstallDirExistMessage`,
  `OsConditionMessage`). CI installs the `wix` dotnet tool instead of using the runner's
  WiX 3.14. The MSI also shrank 67.8 → 53.3 MB.
  **Pinned to WiX 6.0.2 on purpose:** WiX **7** refuses to run until its Open Source
  Maintenance Fee EULA is accepted (`error WIX7015`), so it is unusable in public CI —
  re-evaluate only if that changes.
  The multilingual step was rebuilt on top: WiX 4+ removed `light` and `torch`, so
  `wix-multilang.ps1` now replays jpackage's own `wix build` (recovered from its `--verbose`
  log) per culture and uses the COM `Database.GenerateTransform` in place of `torch`.

## Housekeeping

- [x] ~~**Re-merge decompiled inner classes into their parent files.**~~ — **DONE for the
  whole tree.** Every split-out `Foo$1.java`/`Foo$Bar.java` fragment has been folded back
  into its parent; the source tree now has **zero `$` files** (was 448 when the effort
  began, 861 total files). Anonymous listeners inlined at call sites, named classes nested
  static/inner, all `this$0`/`access$NNN`/`val$` synthetics removed. Covered incrementally:
  WebBrowserTab/MenuBar/WinRegPreferencePage, all of `sancho.core`, `sancho.utility`, the
  `sancho.model.mldonkey` collections, the two JFace viewers, and finally all 93 `sancho.view`
  parents (334 fragments, waves 1–8). Verified by clean full-tree compile at every step. The
  historical detail is below for reference.
  <details><summary>original notes</summary>
  Started at 448 of 861 source files (52%) split-out
  inner classes (`Foo$1.java` anonymous ×322, `Foo$Bar.java` named ×126) across 112
  parents, with 821 synthetic `access$NNN` accessors, 355 files carrying `this$0`, and 74
  `val$` captures. A bulk merge is weeks of work, purely cosmetic, and high-risk (inlining
  the anonymous classes with correct capture / effectively-final semantics is exactly
  where subtle bugs creep in); there is no safe automation (re-decompiling would wipe all
  our fixes/comments/renames). **Do it opportunistically:** when already editing a class,
  fold its small inner classes back inline, rename that file's `varN` to descriptive
  names, and compile-check. The original `0.9.4-23` source is a structural template for
  the ~87 pre-23 parents (`git show 0.9.4-23:<path>`). **Done so far: WebBrowserTab (20),
  MenuBar (26), WinRegPreferencePage (6) — 448 → 390 `$` files. See CHANGELOG.** Good
  next candidates with a -23 template: `ResultTableMenuListener` (18), `LinkRipper` (16),
  `Console` (11), `FileDetailDialog` (10). Note: WebBrowserTab/MenuBar
  still have leftover `varN` in their un-rewritten methods (tidy if revisited).
  **Done since:** the whole `sancho.core` package (`CoreFactory`/`MLDonkeyCore`/
  `MLDonkeyCoreMonitor`/`SSHCoreFactory`/`Sancho`), `sancho.utility` (`SwissArmy`/
  `ObjectMap`/`MyObservable`/`MyObserver`/`VersionInfo`), the `sancho.model.mldonkey`
  collections (`ACollection_Int` + File/Client/Server/Result/Room/Option/Network/
  SharedFile), and both custom JFace viewers (`CustomTableViewer` 12, `CustomTreeViewer`
  14) — all merged **and** fully `varN`-renamed to descriptive names. See CHANGELOG.
  </details>
- [x] ~~**Rename `varN` locals in the rest of `sancho.model`.**~~ — done: all 87 model
  classes outside the already-cleaned collections (domain objects `File`/`Client`/`Result`/
  `Server`/`Network`/`SharedFile`/`Option`/`Room`/`User` + protocol subclasses, the `enums`
  sub-package, and the `utility` wire-format sub-package) renamed to descriptive names.
  `sancho.model` now has zero `varN`; full 747-file compile clean. See CHANGELOG.
- [x] ~~**Rename `varN` locals in `sancho.view`.**~~ — done: all 293 files renamed to
  descriptive names. **The entire source tree now has zero decompiler `varN`** — every
  `sancho.*` package plus `org.eclipse.jface.viewers`. Verified by a clean full-tree
  compile (413 files), a NUL/binary-corruption scan, and a per-file literal audit (every
  string/char literal byte-identical to the previous version). See CHANGELOG. (Done via a
  parallel agent fleet; a first run hit the API session limit mid-way and corrupted two
  files — `CSpinner`'s ` ` escape and `BandwidthDialog` with NUL bytes — which were
  restored from the last good commit and re-renamed cleanly by a second, controlled run.)
- [x] ~~**Validate the Windows associations on an installed MSI build.**~~ — done:
  validated on the `0.9.6` MSI. The exe path resolves to the real `sancho.exe`, the Windows
  Registry page shows the correct per-item state (Sancho / other app, user vs. machine), and
  the startup check prompts only for the unregistered associations and creates them at the
  chosen level.
- [x] ~~**Validate MSI/deb/rpm upgrade over an existing install.**~~ — done: confirmed the
  `0.9.6` MSI upgrades in place over an older installed build, with no "another version of
  this product is already installed" (error 1638). The 0.9.5 fix (incrementing
  ProductVersion) works.
- [ ] **(Evaluated, deferred) Migrate off Trove 2.1.0 — the last unmaintained dependency.**
  Trove is the primitive-collections backbone of the model: `ACollection_Int` wraps a
  `TIntObjectHashMap` (int id → model object) for the 8 core collections (File/Client/
  Server/Result/Room/User/Network/SharedFile), plus `TIntArrayList` for row-index lists
  in the viewers/stats and a few `TLongIntHashMap`/`TObjectIntHashMap`. ~35 files. No CVE,
  stable, isolated, pinned — unlike pircbot there is no security reason to rush.
  **fastutil mapping** (mostly mechanical): `TIntObjectHashMap`→`Int2ObjectOpenHashMap`,
  `TIntArrayList`→`IntArrayList` (`toNativeArray`→`toIntArray`, `getQuick`→`getInt`,
  `reset`→`clear`), `TLongIntHashMap`→`Long2IntOpenHashMap`, etc.
  **Hard part:** fastutil has no `TObjectProcedure`/`TIntObjectProcedure` (boolean
  `execute()` early-stop / retain). 14 classes implement them (`FileCollection$CommitAll`,
  `ServerCollection$RemoveNetworkServers`, `SharedFileCollection$CalculateTotalSize`, …).
  Strategy to minimise churn: define Sancho's own `ObjectProcedure`/`IntObjectProcedure`
  interfaces and reimplement the loop/early-stop/retain once inside `ACollection_Int`/
  `_Hash`; then the 14 classes only swap their `implements`/import, logic unchanged.
  **Effort:** ~1 day + real-core hot-path testing; **risk: medium** (every core message
  hits these maps). **Jar size caveat:** full fastutil is ~23 MB (uber-jar is 6.4 MB) —
  use `fastutil-core`, shade `minimizeJar`, or prefer **HPPC** (~1.7 MB, maintained).
  Recommendation: not worth it now; keep this plan for if/when it becomes necessary.

- [ ] **Decide what `ClientTableView.updateDisplay` should do with the dead
  `downloadsAvailableColor` key** (`ClientTableView.java:74`). The line
  `table.setForeground(PreferenceLoader.loadColor("downloadsAvailableColor"))` is a no-op:
  the key is never registered (no `setDefault`, no color editor — this is its only use), so
  `loadColor` always returns `null` and `table.setForeground(null)` just resets the clients
  table to the default foreground it already has. It looks like a half-wired attempt to reuse
  the "available" highlight concept — the working analog is the *registered*
  `downloadsAvailableFileColor` (editor "p.d.downloads.available" under Display → Downloads,
  used by `DownloadTreeLabelProvider` to tint available files in the downloads tree). Options:
  **(A, recommended) drop the line** — behaviour-identical, removes the confusing dead ref;
  (B) point it at `downloadsAvailableFileColor` — would paint the *whole* clients table text
  in the highlight color (likely unwanted); (C) register a distinct preference + editor for a
  real clients-table text color (a new feature). Deferred by the user for now; needs a visual
  call. NB: unlike `disableUTF8`, this one really is inert.
- [x] ~~**Dead preference keys.**~~ — done: `hm_0_protocol` removed (a registered default
  never read — the host-manager uses `hm_<n>_coreProtocol`). **`disableUTF8` is deliberately
  kept:** it is *not* dead — `SwissArmy` reads it and `ConsoleMessage` uses it to pick the
  console decoding (`getString(false)` vs UTF-8 `getString()`). It is an intentional,
  permanently-`false` hidden escape hatch (settable only by hand-editing the prefs file) for a
  core that sends non-UTF-8 console text. Do not remove it or re-flag it as dead.

- [x] ~~De-duplicate keys in the base `sancho.properties`~~ — done: `mi.dynamicColumn`
  and `mi.sort` (each duplicated with an identical value) collapsed to one definition
  each. Base is 1101 unique keys.
- [x] ~~`mvn package` without `clean` produces a jar missing the resources~~ —
  investigated: **not a Maven bug.** A controlled A/B/C test (fresh build + two
  incremental rebuilds, cleaning via `rm`) puts all 15 root `.properties` (base + 14
  translations) in both the plain and shaded jars every time; `process-resources` copies
  all 558 resources and the change from a marker key propagates through. The earlier
  empty-jar readings were caused by broken cleanup during testing — a PowerShell
  `Remove-Item target\classes` that the sandbox blocked/half-ran (`'\.' is blocked`),
  leaving `target/classes` partial so the jar built from it was incomplete. Plain
  `mvn package` (clean or incremental) is correct; no action needed.

- [~] **Refresh the bundled UI translations.** The keys added during this modernization
  (dialog buttons, PreferenceDialog, the win32-registry message, IRC/web-browser status
  strings) are now translated in **all 14 locales**, and `es_ES` is 100% complete. Still
  outstanding: the keys the original 2004-2006 translators never filled in for the other
  languages (e.g. `fr_FR` ~409, `pt_BR` ~488, `gl_ES` ~433) — those fall back to English.
  Completing them is a large, lower-value translation pass.
- [x] ~~**Prune the leftover `appimage/usr/bin/distrib/` bundle.**~~ — done: removed the
  unreferenced, platform-mismatched files (`sancho.reg`, `preview.sh`/`.bat`,
  `sendalltorrents`, and the Windows/macOS icons `sancho.ico`/`sancho.icns`). Kept the
  license/authorship docs (`AUTHORS`/`LICENSE.txt`/`CPL.txt`/`LGPL.txt`/`README`), the
  upstream `ChangeLog` (linked from README.md and CHANGELOG.md), and the Linux icons
  (`sancho-*.png`/`.xpm`) the legacy AppDir may still use. Verified nothing in the app or the
  AppImage build (`AppRun`/`build.sh`/`sancho-wrapper.sh`) references the removed files.

- [x] ~~Dependabot~~ — done: `.github/dependabot.yml` watches GitHub Actions and
  Maven deps/plugins weekly, ignoring the compatibility-pinned ones (Eclipse SWT,
  which moves with the vendored JFace; Trove major). See CHANGELOG.
- [x] ~~Audit remaining platform code~~ — done: no `_wpf`/platform-suffix files
  remain; fixed macOS external-link opening (checked the removed `carbon` platform
  instead of `cocoa`) and cleaned dead `carbon`/`motif` checks. See CHANGELOG.
- [ ] **macOS: wire the application menu (needs a Mac to test).** No
  `Display.getSystemMenu()` / `SWT.ID_ABOUT` / `ID_PREFERENCES` / `ID_QUIT` hookup, so
  the macOS app-menu About/Preferences and ⌘Q don't route to Sancho's dialogs / shutdown
  (which kills the spawned core). Hook them on cocoa.
- [ ] **macOS: Ctrl+A / Ctrl+F shortcuts detect a control char, not the key.**
  `GTableMenuListener$2` (select/deselect-all) and `Console$4` (find) test
  `character == 1`/`== 6`; the ⌘ modifier now maps via `SWT.MOD1`, but Command doesn't
  emit control characters the way Ctrl does, so these still won't fire on macOS. Switch
  them to `keyCode`-based detection and verify on a Mac.
- [x] ~~Gate the WinReg preference page to win32 only~~ — done: kept the debug
  preview of the page on any platform, but win32-guarded the `regedit.exe` shell-out in
  `WinRegPreferencePage.updateRegistry`, so a debug preview on Linux/macOS no longer
  tries to launch a Windows binary. Unchanged for real Windows users. See CHANGELOG.
- [ ] **Verify bundled icon filename casing on a Linux build.** Linux is case-sensitive;
  a mis-cased icon key that Windows silently resolves would fail. Spot-check `SResources`
  lookups against the asset filenames on Linux.
- [ ] **Enable minimize-to-tray on macOS (needs a Mac to test).** `VersionInfo.hasTray`
  currently excludes `cocoa`, so minimize-to-tray silently no-ops on macOS. SWT
  supports `Tray` on cocoa (menu-bar item); enabling it needs verification on a real
  Mac before flipping it on.
- [x] ~~Reconsider `syncExec` from the core reader thread (deadlock risk)~~ — done:
  `MainWindow`, `FriendsTab` and `GTreeContentProvider` observer callbacks now use
  `asyncExec` (fire-and-forget UI updates, nothing reads the result), matching the
  table content providers. Other `syncExec` sites (CoreFactory dialogs, IRC, link
  ripper) were left as-is — not on the core reader path / need a blocking result.
- [x] ~~Comparator hardening~~ — done: `GSorter` sorts with its own stable merge
  sort that tolerates table data mutating mid-sort, so the global
  `-Djava.util.Arrays.useLegacyMergeSort` JVM flag was removed (launcher, build,
  IDE config). The static comparator-contract bugs it used to mask were also fixed
  (see the sort-crash entry). Merge sort validated for order/stability/tolerance.
