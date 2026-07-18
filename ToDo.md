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

- [ ] **Verify / fix chunk-bar blur on HiDPI (>100% display scaling).** The Downloads
  "chunks" column and the chunk detail dialogs round-trip through
  `ImageData.scaledTo()` (`ChunkImageData` / `ChunkCanvas`), which loses device
  resolution on a scaled monitor (125/150/200%) → soft/blurry bars. Not visible at
  100%. Fix candidates: blit the composed bar with the 8-arg scaled `drawImage`
  instead of pre-scaling via `ImageData`, or at least `gc.setInterpolation(SWT.HIGH)`
  + `setAntialias(SWT.ON)` before the blit. Needs a >100% Windows display to observe.

- [ ] **Verify UTF-8 decoding of core strings (charset fix).** `MessageBuffer.getString`
  now decodes core strings as explicit UTF-8 (was the JVM default charset). Confirm
  against a real MLDonkey core that non-ASCII filenames/nicknames (accents, ñ, …)
  still render correctly on Windows; revert to a different charset if they don't.

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

- [x] ~~Dependabot~~ — done: `.github/dependabot.yml` watches GitHub Actions and
  Maven deps/plugins weekly, ignoring the compatibility-pinned ones (Eclipse SWT,
  which moves with the vendored JFace; Trove major). See CHANGELOG.
- [x] ~~Audit remaining platform code~~ — done: no `_wpf`/platform-suffix files
  remain; fixed macOS external-link opening (checked the removed `carbon` platform
  instead of `cocoa`) and cleaned dead `carbon`/`motif` checks. See CHANGELOG.
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
