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

## Housekeeping

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
- [ ] **Prune the leftover `appimage/usr/bin/distrib/` bundle.** All the `.properties`
  duplicates are gone; the folder still holds legacy docs/icons/`sancho.reg`/preview
  scripts (`preview.sh`/`.bat`, `sendalltorrents`) that nothing in the build references.
  Decide what (if anything) the AppImage still needs and drop the rest.

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
