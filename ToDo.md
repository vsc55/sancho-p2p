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

- [ ] **MSI installer.** Instead of the portable app image, emit a proper
  `.msi`/`.exe` installer (`jpackage --type msi`, needs WiX in CI) with Start Menu
  shortcuts and file associations registered at install time.

## Housekeeping

- [ ] **Dependabot** for SWT/JFace/PircBot/Trove version bumps.
- [ ] **Audit remaining platform code** (`_wpf`, other legacy branches) and remove
  dead paths, as was done for the `_win32` browser.
- [ ] **Comparator hardening (optional).** The legacy merge sort is requested to
  tolerate table data that mutates mid-sort. A "proper" fix would snapshot the
  sort keys before sorting — larger and riskier; only worth it if the VM-level
  mitigation ever proves insufficient.
- [ ] **The local `sancho-0.9.4-59/` folder** (313 MB, the published binary + a
  bundled JRE used for decompilation) is git-ignored. Remove it locally once no
  longer needed.
