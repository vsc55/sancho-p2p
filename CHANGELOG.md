# Changelog

Notable changes to this preservation/modernization of **Sancho**.
Format loosely based on [Keep a Changelog](https://keepachangelog.com/).

The upstream project's original changelog (2004–2006) is preserved at
[`appimage/usr/bin/distrib/ChangeLog`](appimage/usr/bin/distrib/ChangeLog). The
authentic early **0.9.4-23** source lives at the `0.9.4-23` tag
(`git checkout 0.9.4-23`).

## [0.9.12] — 2026-07-24

### Fixed

- **Section-button icons no longer show a black or white box when active or hovered.** The
  highlighted button image is generated at runtime by brightening the base icon's non-transparent
  pixels, but transparency was detected only through the palette's `transparentPixel` (index-based,
  as in indexed GIF/PNG). Modern SWT loads 32-bit RGBA PNGs with an alpha channel instead, so the
  three 32-bit icons (Transfers, Search, Console — the only ones affected) had their transparent
  background painted: a dark box for Transfers/Console, a white one for Search. The generator now
  also honours alpha-channel transparency and preserves per-pixel alpha; indexed icons are unchanged.
- **The tray icon no longer opens a half-built window during startup.** Double-clicking the tray icon
  while the loading splash was still up restored an empty window with no work area — the tray exists
  early (to support starting minimized to it) and the splash pumps the event loop while tabs are
  created, so the click was handled mid-construction. Tray clicks are now ignored until startup has
  finished building the tabs and opening the window.

## [0.9.11] — 2026-07-21

### Fixed

- **Sancho could not see — or repair — a file/URL association left behind by an uninstall.**
  `WinRegAssociations.level()` treated any `shell\open\command` naming `sancho.exe` as correctly
  registered without checking that the executable still exists, so an association pointing at a
  removed or moved install reported as fine: the *Windows Registry* preference page showed it as
  registered and the startup check ignored it (it only offers the ones at `Level.NONE`). The state is
  easy to reach and breaks the association outright — the app registers under `HKCU` (which needs no
  elevation) while the MSI registers under `HKLM`, and since `HKCU` shadows `HKLM`, uninstalling
  leaves a user-level key aimed at a deleted executable that *also* hides the machine-level entry a
  later reinstall writes. Clicking a `.torrent` then silently does nothing. The association level now
  verifies the command's executable is still on disk and reports it as not registered otherwise, so
  the startup check offers to recreate it.

## [0.9.10] — 2026-07-21

### Changed

- **Windows installer toolchain modernized: WiX 3.14 → WiX 6, jpackage JDK 21 → 25.** WiX 3 has been
  end-of-life for years and the build was pinned to it only because the custom installer sources were
  WiX 3 syntax. `packaging/windows/wix/{main.wxs,ShortcutPromptDlg.wxs}` are now WiX 4+ syntax
  (`<Product>` + inner `<Package>` merged; a Component's `<Condition>` child became the `Condition`
  attribute; `Guid="*"` is now implicit; `BinaryRef`; `<Custom>` conditions as attributes), rebased on
  the jpackage 25 templates; the placeholder `overrides.wxi` was dropped in favour of jpackage's own.
  The installer localization files moved to the WiX 4 `v4/wxl` schema and picked up jpackage 25's
  renamed/added strings (`InstallDirNotEmptyDlgInstallDirExistMessage`, `OsConditionMessage`).
  Side benefit: the MSI shrank from **67.8 MB to 53.3 MB**.
- **The multilingual MSI pipeline was rebuilt on WiX 6**, which removed the WiX 3 `light` and `torch`
  that the previous flow used. `tools/wix-multilang.ps1` now recovers jpackage's own `wix build`
  command line from its `--verbose` log and replays it once per culture (changing only `-culture`,
  `-loc` and `-out`) — replaying rather than re-deriving it keeps the ~20 `-d` defines, and the
  `JpProductCode` among them, identical to the base MSI, without which the language transforms would
  be invalid. `torch` is replaced by the WindowsInstaller COM `Database.GenerateTransform`, and a
  shared `-cabcache` avoids recompressing the ~50 MB cabinet for every language. The resulting
  installer is unchanged in behaviour: one file, eight languages, auto-selected by the OS.
- **CI:** the release workflow now sets up JDK 25 and installs the WiX 6 dotnet tool
  (`wix 6.0.2` + the `WixToolset.Util`/`WixToolset.UI` extensions) instead of relying on the runner's
  WiX 3.14. WiX **7** is deliberately not used: it refuses to run until its Open Source Maintenance
  Fee EULA is accepted (`error WIX7015`), which a public CI build cannot do. `actions/upload-artifact`
  moved from v4 to v7, which targets Node 24 (v4 tripped GitHub's Node 20 deprecation warning).
- **CI: packaging changes can be dry-run.** Pushing a tag starting with `test` builds the whole
  Win/Linux/macOS matrix and generates, multilingual-checks and smoke-tests the MSI exactly as a real
  release would, but creates no GitHub Release and uploads to none — the packages are attached to the
  workflow run as artifacts instead. A manual `workflow_dispatch` behaves the same.

### Fixed

- **`build-app.ps1` could package a stale build.** It reused an existing `target/app-input` whenever
  the directory was present, so one Maven build can feed several packages (the Linux job builds `deb`
  and `rpm` back to back). A directory left over from an *earlier version* was reused just the same,
  silently producing an installer that reports the current version while shipping older code. The
  staged input is now discarded unless it holds a jar for the version being built. (Local builds only;
  CI always starts from a clean checkout, so released artifacts were never affected.)
- **The multilingual step could report success after failing to embed anything.** A read-only COM
  handle left open on the base MSI kept the file locked, so the embedding step could not open it for
  writing — yet the build still finished "successfully", producing a single-language installer. The
  handles are now released before embedding, the result is verified afterwards (sub-storage count and
  package language list) instead of trusting the exit code, and the embedding script aborts with a
  non-zero status naming the step that failed.

## [0.9.9] — 2026-07-20

### Added

- **The Windows `.msi` installer is now multilingual — a single file that shows its UI in the
  machine's Windows language.** One `sancho.msi` now carries **English, German, Spanish, French,
  Italian, Japanese, Portuguese (Portugal + Brazil) and Simplified Chinese**; Windows Installer
  applies the embedded transform matching the OS UI language at install time (falling back to
  English), so there is no per-language download and nothing to choose. German/Japanese/Chinese
  reuse jpackage's bundled strings; Spanish/French/Italian/Portuguese are hand-authored in
  `packaging/windows/wix-lang/`, while the standard installer dialogs/buttons come translated
  from WiX's `WixUIExtension`. The build (`tools/build-app.ps1 -Type msi`) does this automatically:
  jpackage builds the base English MSI and, via `--temp`, leaves its WiX intermediates behind, then
  the new `tools/wix-multilang.ps1` re-lights them once per language, generates a language transform
  (`torch`) for each, and embeds the transforms as LCID-named sub-storages with the package language
  list set. Embedding is done by the bundled `tools/wix-embed-transforms.vbs` (WindowsInstaller COM),
  so no Windows SDK sample scripts are needed on the build/CI machine. Adding a language is a new
  `.wxl` in `wix-lang/` plus a row in the script's language table.

### Changed

- **The installer's file/URL association checkbox is now localized.** The "Register .torrent files
  and ed2k:, magnet:, sig2dat: links…" checkbox in the shortcut-options dialog was hardcoded English;
  it is now a `!(loc.SanchoRegisterAssociationsLabel)` token translated in every language's `.wxl`.

## [0.9.8] — 2026-07-20

### Fixed

- **Clicking a client (or download) row could crash with `IndexOutOfBoundsException`**
  and, once guarded, silently lost the selection so the right-click menu had nothing to
  act on. The lazy/virtual Clients and Downloads tables resolved the selection by row
  index against the content provider's `sfList`, which can lag the widget's row count for
  a moment after the sources model changes. `CustomTableViewer.getSelectionFromWidget` now
  resolves the selection from each selected `TableItem`'s stored element (a clicked row is
  always rendered, so its element is present), falling back to the content provider by
  index only for a not-yet-rendered row; `getSFElement` is bounds-guarded in both the table
  and tree content providers, and null elements are skipped when building the tree selection.

### Changed

- **Detail dialogs no longer clip their field labels.** The File/Client detail dialogs gave
  every field-name label a fixed 100px width, so longer (e.g. translated) labels like
  "Visto por última vez" or "Antigüedad de las partes" were cut off. The labels now size to
  their text (the GridLayout column still aligns to the widest label), so the dialog grows to
  fit its content.
- **The column selector and Link Ripper windows are now resizable/maximizable.** `IDSelector`
  (its column table already fills, so a larger window shows more columns) gained a resizable
  shell style; `LinkRipper`'s shell style was spelled out with named `SWT` constants instead
  of the magic literal `2160` and a dead ternary, and gained a maximize button.
- **Performance: dropped `String.intern()` from the hot display formatters and label
  providers.** The size/rate/percent/time formatters in `SwissArmy`, the table label
  providers, and the model's display getters interned every returned string. Those strings
  are transient and mostly unique (they change every refresh), so interning deduplicated
  nothing useful while paying a native string-pool lookup per call on the per-cell/per-refresh
  path and permanently polluting the JVM-wide intern pool. Nothing compares these strings by
  identity, so the calls were pure overhead — removed from 22 files (55 call sites). The
  file-extension dedup and the wire-format intern in `MessageBuffer`, where identical strings
  from the core genuinely repeat, were kept.

## [0.9.7] — 2026-07-19

### Changed

- **Added a `docs/` technical documentation set** (English, with Mermaid diagrams rendered by
  GitHub): overview, architecture (packages, patterns, execution flow), classes + data model,
  MLDonkey protocol + CLI arguments, development guide, and version-history/provenance. Sections are
  numbered by the `docs/README.md` index.
- **Slimmed the root README** into a user-focused landing page: moved the deep
  build/dependency/packaging/CI/AppImage/silent-MSI detail into `docs/DEVELOPMENT.md` and the
  version-history table into `docs/HISTORY.md`, and added a Documentation section linking to `docs/`.
- **Repo housekeeping:** pruned unreferenced, platform-mismatched files from the legacy
  `appimage/usr/bin/distrib/` bundle (`sancho.reg`, `preview.sh`/`.bat`, `sendalltorrents`, the
  Windows/macOS `sancho.ico`/`.icns` icons), keeping the license/authorship docs, the upstream
  `ChangeLog` and the Linux icons; removed the dead `hm_0_protocol` preference default (never read).

### Fixed

- Corrected stale README content: dropped references to the removed IRC client, the abandoned
  `pircbot` dependency, and the deleted `sancho.utility.regex` adapter.
- **VS Code workspace encoding.** `.vscode/settings.json` forced the default (and Java) to
  ISO-8859-1, so UTF-8 files (`.md`, and the 16 `.java` using `—`/`•`/`✓`/`…`) opened as mojibake and
  risked corruption on save. The default is now `utf8`, with ISO-8859-1 scoped to `[properties]` only
  (Java properties must stay Latin-1). Matches the pom's `sourceEncoding = UTF-8`.

## [0.9.6] — 2026-07-19

### Added

- **Windows associations now show their current status and are checked at startup.** The
  Preferences → *Windows Registry* page shows, for each protocol (`ed2k`, `magnet`,
  `sig2dat`, `sfdl`) and the `.torrent` extension, whether it is currently associated —
  with Sancho or with another application — and at which level (user `HKCU` vs. machine
  `HKLM`), checking the per-user hive first. At startup (Windows only, on by default) Sancho
  checks for associations that are completely unregistered and, if any are missing, offers
  to create them at user or machine level — or to stop asking (which turns off the check).
  Associations already owned by another application are left untouched, not flagged. A new
  **Preferences → General** checkbox *"Check Windows associations at startup"* disables the
  check. The registry logic is centralized in a new `WinRegAssociations` helper (shared by
  the page and the startup check). New i18n keys `l.assoc.*` and `p.r.general.checkAssociations`.

## [0.9.5] — 2026-07-19

### Changed

- **Version scheme: plain incrementing releases.** Dropped the decompiler-era
  `0.9.4-<build>` suffix (a build counter frozen on the upstream `0.9.4`) in favor of
  ordinary incrementing versions — `0.9.5`, `0.9.6`, … The version number is now identical
  everywhere: the artifact file name, the in-app *About* dialog, and the installer's
  internal version.

### Fixed

- **Installers refused to upgrade an existing install.** Every `0.9.4-<build>` package
  carried the *same* numeric version (`0.9.4`; the `-<build>` part only lived in the file
  name), so once any build was installed, installing a different one failed — Windows
  Installer aborted with *"another version of this product is already installed"* (error
  1638), and apt/dnf saw no newer version. jpackage's `--app-version` now comes from the
  real, plain-incrementing project version, so the MSI `ProductVersion` (and the deb/rpm
  version) rises every release and a new build installs cleanly over the previous one.

## [0.9.4-77] — 2026-07-19

### Fixed

- **Windows file/protocol association did nothing on an installed (MSI) build.** The
  `.reg` file was written to `user.dir`, which for an installed launcher is the
  non-writable `C:\Program Files\…\Sancho` directory, so `FileOutputStream` threw *Access
  Denied* — swallowed by a silent `catch`, so no dialog appeared and nothing was
  registered (it only worked when run from a writable dev directory). It now writes the
  `.reg` to `java.io.tmpdir` (always writable) and surfaces a warning dialog if creation
  still fails. The executable path already resolves to the real `sancho.exe` via
  `jpackage.app-path`.
- **A failed screenshot save disappeared silently.** The Tools → Screenshot save
  (`ImageLoader.save` to a user-chosen path) had no `catch`, so a write failure (e.g. a
  non-writable target) produced no feedback; it now reports the error in a dialog. (Found
  while auditing for other permission-prone file writes — the registry `.reg` was the only
  one writing to a non-writable install-relative path; everything else writes under the
  writable user home directory.) New i18n key `l.saveFailed`.

## [0.9.4-76] — 2026-07-19

### Added

- **The Advanced-search "Format" dropdown is now configurable.** Its options were hardcoded
  in `SearchTab_Advanced` (`exe`/`bin`/`img`/`gif`/`jpg`); they now come from a new
  `searchFormats` preference (default `exe;bin;img;gif;jpg;mkv;mp4` — `mkv`/`mp4` added)
  editable under **Preferences → Display → Search** as a `;`-separated list. The advanced
  search tab rebuilds the dropdown from the preference and refreshes live when preferences
  are applied (no restart needed), preserving whatever the user currently has selected; a
  leading "" (no-format) entry is always kept. New i18n key `p.d.search.formats` (English
  base + `es_ES`).
- **The "Check for updates" feature now targets this fork's GitHub releases.** The old
  `http://sancho.awardspace.com/version.php` host is long dead, so `VersionChecker` now
  queries `https://api.github.com/repos/vsc55/sancho-p2p/releases/latest` over HTTPS (with
  connect/read timeouts, the GitHub-required `User-Agent`, and `Accept: application/vnd.github+json`),
  parses the release `tag_name`, and the "Visit" button opens the GitHub releases page.
  Triggering the check from **Help → Check version** is now *interactive*: it shows an
  explicit message box for every outcome — up to date, a newer release (dialog with a
  Visit button), or an error — instead of silently updating only the status line. The
  automatic start-up check stays quiet (status line, plus a pop-up only when a newer
  version exists and the pop-up preference is on). New i18n key `l.upToDate`.

### Changed

- **Renamed the decompiler's `var1`/`var2`/… locals to descriptive names across the rest
  of `sancho.model`** — the 87 model classes outside the already-cleaned collections:
  the domain objects (`File`, `Client`, `Result`, `Server`, `Network`, `SharedFile`,
  `Option`, `Room`, `User` and their protocol-version subclasses like `File41`/`Client19`),
  the `enums` sub-package (hand-rolled `AbstractEnum` + `EnumNetwork`/`EnumTagType`/…), and
  the `utility` wire-format sub-package (`MessageBuffer`, `MessageEncoder`, `Query`,
  `SearchQuery`, `Addr`, `Tag`, `Format`/`Format_OGx`, the `NetworkStat` trio, …). Only
  locals, parameters, catch/loop variables were renamed — no fields, method/class names,
  control flow, or literals changed. `sancho.model` now has zero `varN`. No behaviour
  change (verified: full 747-file compile clean).
- **Folded every remaining decompiled inner-class file in `sancho.view` back into its
  parent** — 334 split-out `Foo$1.java` / `Foo$Bar.java` fragments across 93 parent
  classes, completing the inner-class re-merge started earlier for `WebBrowserTab`/`MenuBar`/
  `sancho.core`/`sancho.utility`/the model collections/the JFace viewers. Anonymous
  listeners are inlined at their call sites as `new Type(){…}`; named classes
  (menu-item `Action`s, dialogs, comparators, view frames, stream monitors, …) become
  nested `static`/inner classes; the decompiler's `this$0` back-references, ~hundreds of
  synthetic `access$NNN` accessors, and `val$` captures are removed (loop-captured or
  name-colliding locals get an effectively-final descriptive copy). Cross-file references
  to a nested named type (e.g. `HostPage.TextLabel` from `SSHHostPage`) were updated.
  The `0.9.4-23` original was used as a structural template where available (72 of 93
  parents). **The entire source tree now has zero split-out inner-class (`$`) files**
  (down from 448 when the re-merge began). Genuine `Class.forName`/`class$` idiom fields
  are left intact. No behaviour change; verified by a clean full-tree compile (861→413
  source files). Existing `varN` locals inside un-restructured method bodies are left for
  a follow-up descriptive-rename pass (see ToDo).
- **Renamed the decompiler's `varN` locals to descriptive names across all of `sancho.view`**
  — 293 files, completing the descriptive-rename pass over the last package that still used
  them. Only local variables, parameters, catch-clause and loop variables were renamed
  (from their type and usage: `Composite`→`composite`, `SelectionEvent`→`event`,
  `MessageBuffer`→`buffer`, loops→`i`/`j`/`k`, `catch (IOException …)`→`ioException`, …);
  no fields, method/class names, the `class$` idiom, literals, escape sequences, imports,
  control flow, or comments were touched. **The entire source tree now has zero decompiler
  `varN` names** (every `sancho.*` package plus `org.eclipse.jface.viewers`). No behaviour
  change, verified three ways: a clean full-tree compile (413 files), a NUL/binary-corruption
  scan (none), and a per-file audit confirming every string/char literal is byte-identical
  to the previous version (no escape like ` `/` ` was altered).

- **Preferences dialog: MLDonkey core option sections are grouped and localized.** The
  left-hand tree used the core's raw English section names (`Bandwidth`, `Networks`, …).
  They now appear under a localizable `Core:` / `Núcleo:` prefix (mirroring the `sancho:`
  pages) with translated section names where available (`Bandwidth`→`Ancho de banda`,
  `Main`→`General`, …), falling back to the raw core name for plugin / network / unknown
  sections; the synthetic "Advanced" and "All" views get the same prefix. New i18n keys
  `p.node.section.*` (English base + `es_ES`).

### Fixed

- **The Preferences dialog's Restore-Defaults / Apply buttons were English on the core-option
  pages, and the "Restore Defaults" label was clipped.** They are now relabelled from Sancho's
  translations on every page type — not just the `sancho:` `CPreferencePage`s but also the
  `MLDonkeyPreferencePage` core pages — and each button is widened to fit its translated text.
- **The "Popup on new version" preference is disabled while the automatic version check is
  off.** The two checkboxes were independent, so the pop-up option looked meaningful even when
  it had no effect; it now greys out and tracks the "Notify me…" checkbox live (and after
  Restore Defaults).
- **Spanish `ToolItem` labels are translated.** The Web-browser and Console shortcut fields
  (and the "ToolItems" tab) showed the raw SWT term `ToolItem`; the `es_ES` labels now read
  "Botón" / "Botones".

## [0.9.4-75] — 2026-07-19

### Fixed

- **Windows file/protocol association registration required administrator and gave no
  feedback.** The page wrote to `HKEY_CLASSES_ROOT` (machine-wide → needs elevation) and
  ran `regedit.exe /s` silently — and regedit's manifest forces elevation, so it failed
  without admin even for a per-user write. It now imports via `reg.exe import` (which runs
  at the caller's level) and defaults to a **per-user** registration
  (`HKEY_CURRENT_USER\Software\Classes`, no administrator needed), with a "Register for
  all users (requires administrator)" checkbox for the machine-wide behaviour, and reports
  success or failure from the exit code. The generated `.reg` also had a broken
  backslash-doubling (a gnu.regexp→java.util.regex regression: `SwissArmy.replaceAll(p,
  "\\\\","\\\\")` was a no-op), so the executable path came out with single backslashes
  that the import mangled — now doubled with `String.replace`. The executable path is
  taken from jpackage's `jpackage.app-path` when installed, so the association points at
  the real `sancho.exe`.

### Changed

- **Folded decompiled inner classes back into their parent files** for `WebBrowserTab`
  (20 fragments), `MenuBar` (26), `WinRegPreferencePage` (6), and the whole `sancho.core`
  package — `CoreFactory` (9), `MLDonkeyCore` (1), `SSHCoreFactory` (2), `Sancho` (3) —
  plus `SwissArmy` (2) in `sancho.utility` and the 19 Trove-procedure classes across the
  `sancho.model.mldonkey` collections (`ACollection_Int`, `File`/`Client`/`Server`/
  `Network`/`Result`/`Room`/`Option`/`SharedFileCollection`), and the two custom JFace
  viewers in `org.eclipse.jface.viewers` — `CustomTableViewer` (12 anonymous listeners)
  and `CustomTreeViewer` (14) — so `sancho.core`, `sancho.utility`, the
  `sancho.model.mldonkey` collections and `org.eclipse.jface.viewers` now have no split-out
  inner-class files. Purely structural: anonymous listeners are inlined at their call sites, named
  classes (`WebBrowserViewFrame`, the favorite `Action`s, `AlphaInputDialog`,
  `URLListener`, `RegisterLink`/`RegisterExtension`) become nested classes, and the
  decompiler's `this$0`/`access$NNN` artifacts are removed. Loop-captured variables get
  an effectively-final copy where a listener needed it. The `0.9.4-23` original was used
  as a structural template where available. No behaviour change; the first, opportunistic
  steps of the inner-class re-merge (see ToDo). Verified: browser tab and all menus work.
- **Renamed the decompiler's `var1`/`var2`/… locals to descriptive names** across the whole
  `sancho.core` package (`ICore`, `MLDonkeyCore`, `MLDonkeyCoreMonitor`, `SSHCoreFactory`,
  `Sancho`) — e.g. `processMessage(int opcode, MessageBuffer buffer)`, `send(short opcode,
  Object[] args)`, the SSH proxy setup (`proxyHost`/`proxyPort`/`proxy`/`proxyPass`), and
  `Sancho`'s argument parsing (`arg`/`flag`/`value`) — and the whole `sancho.utility` package
  (`SwissArmy`, `ObjectMap`, `MyObservable`, `MyObserver`, `VersionInfo`), e.g.
  `calcStringSize(long size)`, `replaceAll(input, regex, replacement)`, `fileToByteArray`,
  the link/regex helpers, and the observer plumbing (`observer`/`arg`/`flags`). Also
  modernized `MLDonkeyCore`'s decompiled `Class.forName`/`class$` idiom for the active-tab
  check into a plain `instanceof SharesTab` / `instanceof TransferTab`, removing the two
  synthetic `class$…` fields and the synthetic `class$()` method. No behaviour change.

### Removed

- **The IRC client.** Sancho's built-in IRC window (server/channel console, channel-user
  list, connect dialog) was dropped, which removes the last unmaintained dependency:
  `pircbot:pircbot:1.5.0` (abandoned since ~2011, a parser for untrusted IRC server text).
  Deleted the `sancho.view.irc` package, the `IRCConsole` classes, the Tools → "IRC
  client" menu item, the IRC preference tabs (display colors + server/nick/channel), all
  `irc*` preference defaults, the IRC i18n keys across all 14 locales, and the `pircbot`
  Maven dependency. The uber-jar no longer bundles any pircbot/jibble classes. (The only
  remaining old dependency is Trove 2.1.0, which is stable, isolated, and pinned.)

## [0.9.4-74] — 2026-07-18

### Fixed

- **Size/rate numbers could intermittently corrupt or crash (data race).**
  `SwissArmy.calcStringSize` / `calcStringSizeGrouped` weren't `synchronized` yet shared
  the static `DecimalFormat` (`df000`/`df00`/`dfGrouped`) and `FieldPosition` with the
  already-synchronized `calcRate`/`percentToString`. The core reader thread formats sizes
  (shared-file totals, upload table) while the UI thread formats table columns, so the two
  raced on the non-thread-safe formatters → occasional `ArrayIndexOutOfBoundsException`
  from `DigitList` or garbled numbers. Both methods are now `synchronized` on the same
  monitor.
- **"Update Registry" on the File Extensions tab crashed.** `changedExtPrefs` looped over
  `registerLinks.length` (4) while indexing `registerExtensions` (length 1) →
  `ArrayIndexOutOfBoundsException` when the ".torrent" option was left unchanged. Bounded
  by the array actually indexed.
- **A malformed line in the download log crashed the Download-Complete window.**
  `DownloadCompleteItem.parseLine` sliced with an unchecked `indexOf("|")` result; a
  truncated `ed2k://` line (`indexOf` → -1) threw `StringIndexOutOfBoundsException`. The
  separators are now bounds-checked.
- **Chunk-image cache grew without bound.** The downloads tree cached a `ChunkImageData`
  per row in the label provider but the viewer pruned a different, never-populated field,
  so removed/finished downloads leaked their chunk buffers for the whole session. The
  prune now targets the real cache.
- **ETA-column sort was unstable** with two or more empty-ETA rows (`compare` violated
  antisymmetry — the case-8 fix pattern was missing here); empty ETAs now compare equal.
- **Robustness guards:** a null option value no longer NPEs the advanced-options page
  (`isBoolean(null)`); the network-stats label provider no longer NPEs on dispose before
  first render; the tray tooltip/restore paths null-check `trayItem` (consistent with the
  tray-less-platform fallback). Removed a stray decompiled `drawText("is the time", …)`
  from the download name column and a dead misspelled `useGraident` preference default;
  the chunk detail canvas now repaints on resize.

## [0.9.4-73] — 2026-07-18

### Fixed

- **Dialog buttons and the Preferences window stayed English.** Sancho's own dialogs
  labelled their buttons from JFace's English `IDialogConstants` (OK/Cancel/Yes/No), and
  the JFace `PreferenceDialog` supplied its own English title and OK/Cancel/Apply/Restore-
  Defaults labels — none of which follow Sancho's locale. All 17 dialog buttons now pull
  from `SResources` (`b.ok`/`b.cancel`/`b.yes`/`b.no`), and a `SanchoPreferenceDialog`
  subclass plus a `CPreferencePage` hook relabel the Preferences window's title and its
  OK/Cancel/Apply/Restore-Defaults buttons from `SResources` too. A handful of hardcoded
  status/error strings (IRC connect/disconnect titles, the web-browser-launch failure,
  the version-check-unavailable line, the link-ripper "Found links" label) were routed
  through `SResources` as well. The new keys are translated across all 14 bundled
  locales, and `es_ES` was completed to 100% of the current key set.
- **UI translations never loaded — the app was always English.** The 14 bundled
  translations (`sancho_<locale>.properties`) lived only in `appimage/usr/bin/distrib/`,
  which nothing ships: the Maven build bundled only the English base, and `SResources`
  looked for a translation solely in the user's home dir (`~/.sancho/`), where no build
  or installer step ever placed one. The translations now ship on the classpath inside
  the jar, and `SResources` resolves the chosen locale from there (with the English base
  as the per-key fallback and a `~/.sancho/sancho_<locale>.properties` still able to
  override). Language is driven strictly by the `locale` preference — an unset/unknown
  locale stays on the English base rather than following the OS locale. The language
  picker in Preferences → General now lists the locales bundled in the jar (plus any
  home-dir override) instead of scanning only the home dir, so it is no longer empty. The
  `es_ES` (Spanish) translation was completed to 100% of the current keys. The stale
  duplicates under `appimage/usr/bin/distrib/` were removed.
- **Windows Registry preference page tried to run `regedit.exe` off Windows.** In debug
  builds the page also shows on Linux/macOS (for UI preview), where its "Update Registry"
  button shelled `regedit.exe` — a Windows binary. The `updateRegistry` shell-out is now
  win32-guarded (off Windows it shows a "only supported on Windows" message instead), so
  the page stays previewable in debug but only touches the registry on Windows (unchanged
  for real Windows users).
- **Web browser: the address bar could show the wrong tab's URL.**
  `WebBrowserTab.inputCombo` was a single field pointing at the last-created tab, so
  with multiple browser tabs the URL was written to (and read from) that tab's combo
  instead of the selected one. Each combo is now bound to its `CTabItem` and every
  read/write resolves the right tab's combo.
- **Web browser: dropped non-ASCII URLs were mangled.**
  `UniformResourceLocator.nativeToJava` decoded the dropped bytes with the JVM default
  charset (UTF-8 since JDK 18). It now decodes the Windows shell `UniformResourceLocator`
  format as `windows-1252` and the Gecko `text/x-moz-url-data` format as UTF-16LE (the
  latter was also being truncated to one character by a scan-to-first-NUL).

### Removed

- **Web browser: dead "grab page as .torrent" Ctrl+click path.** `ctrlDown()` was
  hard-wired to `false` and `setupCtrlKey()` was an empty method with no callers, so the
  Ctrl branch never ran; SWT doesn't reliably deliver key events over a native browser
  control, so the feature can't be resurrected as designed. Removed the dead methods and
  the unreachable branch.

## [0.9.4-72] — 2026-07-18

### Fixed

- **Client-stats network list could be misparsed (proto ≥ 18).**
  `ClientStats18.readNetworks` read the per-network "connected servers" count only
  when the network id was already registered, but that `int32` is always on the wire —
  so an unknown id (common during startup/reconnect, before the matching
  `Network_info` arrives) left the count unread and every remaining entry in that
  stats tick was parsed one field out of alignment. Both fields are now read
  unconditionally. Self-healed on the next tick before, but showed wrong per-network
  server counts meanwhile.
- **Preview `Range` header could crash with an off-by-one.** `File.getContentRange` /
  `SharedFile.getContentRange` guarded the subfile index with `<= length` then indexed
  `[index]`, throwing `ArrayIndexOutOfBoundsException` when the index equalled the
  subfile count. Changed to `< length`.
- **System tray crashed startup on tray-less Linux (GNOME/Wayland).**
  `Display.getSystemTray()` returns `null` on platforms `hasTray()` still green-lights,
  and `new TrayItem(null, 0)` throws `ERROR_NULL_ARGUMENT` — the old `trayItem == null`
  guard was dead code (a constructor never returns null), so the app died with no
  window. It now checks the actual tray and falls back to plain window minimize.
  (Windows always has a tray, so it was never hit there.)
- **Tray context menu leaked a native `Menu` on every right-click.** `MinimizerTray`
  called `MenuManager.createContextMenu` per `MenuDetect`, minting a fresh menu each
  time; it's now built once and reused (the listener still rebuilds the items on show).
  The tray icon is also disposed on shutdown.
- **File preview crashed on Linux/macOS.** `File.repSep` normalised path separators via
  `SwissArmy.replaceAll`, which compiles its "from" argument as a regex — a lone `"\\"`
  threw `PatternSyntaxException` and then NPE'd on `/`-separator platforms. Switched to
  literal `String.replace`.
- **Keyboard shortcuts now use the platform modifier (macOS ⌘).** Ctrl+A/Ctrl+F/Ctrl+T
  and the tab/link accelerators hardcoded the Ctrl bit; they use `SWT.MOD1` now
  (unchanged on Windows/Linux, Command on macOS).
- **`MessageEncoder.toBytes(Long)`** still used the signed `% / /= 256` division that was
  already fixed for `Short`/`Integer`; a uint64 with bit 63 set serialised only its low
  byte. Switched to bit-shift. (Latent — no current message sends a `Long`.)
- **Browser tab title could throw and stop updating.** The `TitleListener`
  dereferenced the `CTabItem` before its own null check; the Edge/WebView2 backend can
  fire a title event before the tab data is set, causing an NPE (and the title/label
  froze). It now null-checks first.
- **Dragging a link/text that offered only "copy" did nothing.** Three drop targets
  (browser URL combo, the `createLinkDropTarget` helper, the link-ripper field) forced
  `DROP_LINK`, which SWT rejects when the source only advertises `DROP_COPY`, so the
  drop was silently discarded. They now honour the offered operations.
- **Keypad Enter in the browser address bar** was compared against `character` (always
  false) instead of `keyCode`; corrected (was harmless — the normal Enter clause
  already covered it).
- **Cancelling a preview transfer froze the GUI up to 4 s.** `TransferDialog.close()`
  busy-waited on the UI thread for the download thread to acknowledge the cancel; it
  now just signals cancel and closes (the download thread stops on its next chunk and
  only touches widgets through disposed-guarded callbacks).
- **Adding a local `.torrent` froze the GUI ~1 s per file.** `SwissArmy.sendLocalTorrent`
  slept a second on the UI thread after the send was already flushed; removed.
- **A server added on a high port (32768–65535) was sent the wrong port.**
  `MessageEncoder.toBytes(Short)` used signed `/ 256` division, which mangled the high
  byte for ports that narrow to a negative `short`; it now extracts the bytes bitwise.
- **"Edit MP3 tags" and the shared-file detail dialog didn't respond to Enter** (no
  default button); OK is now the default in both.
- **The GUI froze for up to 3 s on an incoming friend message.** `FriendsTab`
  resolved the sender with a retry loop that `Thread.sleep`s up to 3× — and it ran
  inside the UI-thread runnable, freezing the whole GUI when the message came from a
  not-yet-collected client. The resolution (with its sleep) now runs on the core
  thread and only the UI append is marshalled.
- **"Add Friend" dialog didn't submit on Enter** (its hand-rolled button bar set no
  default button); OK is now the default.
- **Closing an IRC channel during traffic could throw "Widget is disposed".** The
  coloured-message runnable (`IRCConsole`) guarded the append but not the styling
  that followed; it now returns early if the console is disposed.
- **IRC voice/devoice was broken.** `CUser.voice()` compared for `@` twice (so a
  user could be voiced twice → `++nick`), and `deVoice()` used `>= 1` where the
  voiced prefix `+` is at index 0, so `-v` never removed the voice. Both corrected.
- **"Core Verbosity" dialog discarded changes on Enter.** Cancel was the default
  button (same bug as the IRC Connect dialog), so pressing Enter cancelled instead of
  applying the ticked verbosity keywords. OK is now the default.
- **The "Update stats tables delay" preference did nothing.** Its editor wrote to a
  dead key (`statsUpdateDelay`) while the core reads `statsDelay`, so the setting
  never took effect (and showed 0 instead of the 600 s default). The editor now uses
  the correct key.
- **Splash progress guarded against an out-of-range index** (`on[]` is shorter than
  `boxes[]`), avoiding a possible `ArrayIndexOutOfBounds`.
- **Base64 broke on inputs over 127 bytes.** `Base64.encode`/`decode` used `byte`
  loop counters and a `byte` output index that overflowed past 127, crashing (array
  index out of bounds) on larger data — e.g. HTTP Basic-auth credentials or a long
  preference key. Now use `int`.
- **Statistics graph-history window hung the UI.** `GraphHistory` drew the vertical
  grid with a `byte` loop counter that overflowed past 127 (120 + 20 → −116), so the
  loop never ended once the window was ≥128 px wide — an infinite loop in the paint
  handler that froze Sancho. Counter is now an `int`.
- **First paint of a statistics graph could throw.** `GraphCanvas.paintControl`
  assumed `controlResized` always ran before the first paint (only that set
  `needNewBuffer`); on modern SWT it may not, leaving `imageBuffer` null and
  `new GC(null)` throwing. It now also builds the buffer when it's null.
- **Downloads/Transfers tree could show stale rows after sorting/filtering** — the
  same virtual-viewer gap fixed for tables in 0.9.4-71, found by auditing the
  sibling `CustomTreeViewer`. Its `myClear` relied on SWT lazy `SetData` re-firing
  (unreliable on modern SWT/JFace), so top-level rows could keep pre-sort content.
  It now explicitly renders the visible top-level rows from the freshly-sorted
  content provider, mirroring the table fix.

## [0.9.4-71] — 2026-07-18

### Added

- **IRC server field in the "Connect to IRC?" dialog.** You can now set the server
  (pref `ircServer`) there alongside the nickname and channel, instead of only in
  Preferences.

### Fixed

- **Tables showed stale/duplicated rows and some rows ignored sorting.** The custom
  virtual-table viewer never rendered rows that SWT asked for lazily — its
  `ILazyContentProvider.updateElement(int)` was empty and a never-rendered row had no
  item mapping, so the positional-diff refresh could never repaint it. Rows (often
  the top ones) kept stale content and "stuck" when sorting/filtering; the same
  element could appear in two rows. The Servers "Connected" view was the most
  visible case (it showed wrong servers). Fixed by implementing the lazy
  `updateElement(int)`, explicitly rendering the visible rows from the freshly
  sorted content in `myClear`, and restoring the stale-item disassociation guard in
  `replace` (which the sibling `CustomTreeViewer` still had). Affects every virtual
  table (transfers, servers, uploads, search results, …). Diagnosed with runtime
  logging against a live core.
- **IRC rejected nicknames with accents/ñ** ("432 Erroneous Nickname"). The nickname
  is now sanitized before connecting — accents are decomposed (ñ→n, á→a) and only
  IRC-legal characters are kept — so a configured nick like `pruebañ…` connects as
  `prueban…` instead of failing.
- **"Connect to IRC?" dialog cancelled on Enter.** Cancel was the default button, so
  pressing Enter after editing a field cancelled instead of accepting. OK is now the
  default button, and the edited nick/channel are only saved on OK (Cancel discards
  them).

## [0.9.4-70] — 2026-07-18

### Fixed

- **Core would not start from a path with spaces** (e.g. `C:\Program Files\…`).
  `ExecConsole` used the single-string `Runtime.exec`, which splits on whitespace;
  it now uses the `String[]` form (and tolerates a null parent dir).
- **External links were dead on modern Linux.** The GTK path spawned
  `mozilla`/`konqueror`/`netscape` (none of which exist today). It now opens links
  via SWT's `Program.launch` (xdg-open/gio), falling back to `xdg-open` and then the
  legacy chain, while still honouring an explicitly configured browser.
- **Sorting could crash with "Comparison method violates its general contract".**
  Three comparators were not antisymmetric for equal elements — `Addr` (two
  firewalled peers), client State (two downloading clients), and the downloads tree
  Rate/State column (files sharing a state). All now return a consistent order, so
  sorting those columns no longer throws on TimSort.
- **Integers with the high bit set were corrupted on the wire.**
  `MessageEncoder` masked the top byte with `0x7FFFFFFF`, dropping bit 31; large
  ids / IP-as-int values sent to the core are now encoded correctly.
- **A malformed chunk-age token from the core broke the read loop.**
  `File.readChunkAges` now guards `Integer.parseInt` (like its sibling `readAge`)
  and defaults to 0 instead of throwing out of the socket message stream.
- **Size filters in "TB" evaluated to 0 bytes.** `SwissArmy.stringSizeToLong` used
  an `int` multiplier, so 2^40 overflowed to 0; it now uses a `long` (and `double`
  arithmetic for precision).
- **Core strings decoded with the JVM default charset** rendered differently across
  JDK 17 vs 18+ (mojibake on non-ASCII filenames/nicknames). `MessageBuffer` now
  decodes them as explicit UTF-8.
- **GDI handle leak browsing search results.** The result tooltip allocated a new
  `Region` on every hover without disposing the previous one; it is now disposed on
  each reposition and on teardown.
- **Native bitmap leak on every screenshot.** Tools → Screenshot never disposed the
  window-sized `Image` (on save or cancel); it is now disposed in a `finally`.
- **False "new version" popup from the dead update host.** The version check now
  validates that the fetched string looks like a version before showing the
  "latest version" text / popup, avoiding a bogus prompt (and a null-line NPE).
- **Minor robustness/leak cleanups.** `Console` now disposes its hand `Cursor`
  (leaked one handle per console); `GSorter.compareInts` uses `Integer.compare`
  (no subtraction overflow) and the string tie-break honours the working sort
  direction; `MessageBuffer.getString` bounds-checks the length field before
  decoding (a malformed length no longer throws out of the read loop); and the
  debug `hexDump`/`getLastMessage` no longer drop the last byte or wrap the offset
  label.
- **Core reader thread no longer blocks on the UI thread.** The `MainWindow`,
  `FriendsTab` and `GTreeContentProvider` observer callbacks used `Display.syncExec`
  from the socket reader thread (a potential deadlock); they now use `asyncExec`
  like the other content providers.
- **Removed the dead Windows 95/98/ME detection.** `VersionInfo.isWin95` /
  `isOldWindows` / `oldWindows` never matched a modern `os.version`, so the legacy
  small-icon and emulated-spinner branches were unreachable; dropped them (`BSpinner`
  now always uses the native SWT `Spinner`).

### Changed

- **Regexes now use `java.util.regex` directly.** The thin `sancho.utility.regex`
  adapter (`RE`/`REMatch`/`REException`) kept when `gnu-regexp` was dropped is gone;
  its 13 call sites were rewritten to `Pattern`/`Matcher`/`PatternSyntaxException`
  (a 1:1, behaviour-preserving mapping — exactly-one-match and null-input semantics
  kept). Verified the link/bookmark/env-var parsing conversions and a full build.
- **Table sort tolerance no longer needs a global JVM flag.** `GSorter` now sorts
  with its own stable merge sort, which tolerates the live model data being mutated
  by the core thread mid-sort (the reason a plain `Arrays.sort`/TimSort threw
  "Comparison method violates its general contract"). The
  `-Djava.util.Arrays.useLegacyMergeSort=true` flag was therefore removed from the
  launcher, `build-app.ps1` and the VS Code launch config. Validated the merge sort
  against `Arrays.sort` (order + stability) and confirmed it tolerates an
  inconsistent comparator without throwing.
- **Dropped the deprecated-for-removal boxing constructors.** Replaced all 77
  `new Integer(…)` / `new Short(…)` / `new Byte(…)` calls (inherited from the
  decompiled source) with the `valueOf(…)` factory methods, which are the
  non-deprecated, allocation-free form. No behavioural change — these values are
  used as core message payloads and `HashMap` keys, both compared by `equals`.

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
