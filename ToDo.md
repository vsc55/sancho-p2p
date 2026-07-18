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
- [ ] **Pure `java.util.regex` migration (optional).** The above kept a thin
  `RE`/`REMatch` adapter (~24 call sites unchanged) to minimize risk. A cleaner
  follow-up is to rewrite those sites to use `Pattern`/`Matcher` directly and drop
  the adapter — best done once functional tests back the link-parsing paths.

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
- [ ] **Comparator hardening (optional).** The legacy merge sort is requested to
  tolerate table data that mutates mid-sort. A "proper" fix would snapshot the
  sort keys before sorting — larger and riskier; only worth it if the VM-level
  mitigation ever proves insufficient.
