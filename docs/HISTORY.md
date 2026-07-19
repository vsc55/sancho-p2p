# Version history & provenance

[← Index](README.md) · [Architecture](ARCHITECTURE.md) · [Classes](CLASSES.md) · [API](API.md) · [Development](DEVELOPMENT.md)

> Per-release change detail is in [`../CHANGELOG.md`](../CHANGELOG.md) and on the
> [Releases](https://github.com/vsc55/sancho-p2p/releases) page (both authoritative). This page
> records the version lineage and the decompilation provenance.

## 6.1 Provenance

`sancho-src/src` on `main` is **not** authentic source. It was recovered by decompiling the last
published `0.9.4-59` binary (Vineflower) and then ported to modern SWT/JFace. It includes features that
were recovered by decompilation with no preserved authentic source (web browser, network stats, file
comments; the decompiled IRC client was later removed). For the genuine early source, use the
`0.9.4-23` tag.

**Reconstruction caveats:**

- The GCJ-only `Prov` bootstrap is a no-op stub.
- `MyMenuManager` / dynamic field-editor clearing use reflection over JFace internals.
- The removed SWT Mozilla backend is mapped to Edge on Windows.

## 6.2 Version lineage

Sancho used a `0.9.4-NN` snapshot scheme, later switched to plain-incrementing versions
(`0.9.5`, `0.9.6`, …). `main` always holds the newest modernized build.

| Version | What it is | Where |
| --- | --- | --- |
| `0.9.4-23` | Early **authentic** hand-written source | [`0.9.4-23`](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-23) tag — `git checkout 0.9.4-23` |
| `0.9.4-59` | Last **published** binary (source lost); recovered by decompilation | [`0.9.4-59`](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-59) tag / `decompiled-0.9.4-59` branch |
| `0.9.4-60` | First **modernized** build: decompiled `-59` ported to modern SWT/JFace + JDK 17, plus bug fixes | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-60) |
| `0.9.4-61` | Maintained **JSch** fork + CI build check | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-61) |
| `0.9.4-62` | **Cross-platform** release artifacts (Windows / Linux / macOS) | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-62) |
| `0.9.4-63` | Self-contained **uber-jar** (one-file `java -jar`) | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-63) |
| `0.9.4-64` | Dropped `gnu-regexp` for the JDK's `java.util.regex` | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-64) |
| `0.9.4-65` | Release pipeline off deprecated Node-20 artifact actions | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-65) |
| `0.9.4-66` | macOS external-link fix, platform-code cleanup, Dependabot | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-66) |
| `0.9.4-67` | Windows **`.msi` installer** (optional association prompt + silent-install support) | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-67) |
| `0.9.4-68` | Cleaner build output (shade + checksum warnings), serialized release runs | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-68) |
| `0.9.4-69` | Startup toolbar fix, real build version in the title, Edge web browser on Windows | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-69) |
| `0.9.4-70` | Latent-bug sweep (core spawn, Linux links, sort contracts, leaks), local stable sort, pure `java.util.regex` | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-70) |
| `0.9.4-71` | Virtual-table render fix (stale/duplicate/stuck rows), IRC nick + connect-dialog fixes | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-71) |
| `0.9.4-72` | Audit sweep: tree render, UI-freeze fixes, numeric overflows, ~6 dialog defaults, IRC, browser/DnD, tray & protocol-parsing regressions, cross-platform (macOS ⌘, Linux preview/tray) | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-72) |
| `0.9.4-73` | Working localization: translations bundled in the jar, language picker fixed, dialog buttons + Preferences window + stray strings routed through i18n, all 14 locales updated (es_ES 100%) | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-73) |
| `0.9.4-74` | Audit fixes: DecimalFormat data race, Windows-registry crash, Download-Complete parse crash, chunk-image cache leak, ETA sort + robustness guards | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-74) |
| `0.9.4-75` | **IRC client removed** (drops the abandoned `pircbot` dependency); decompiler cleanup begun — inner classes merged back + descriptive variable names across `sancho.core`/`utility`/`model` + JFace viewers; per-user Windows association via `reg.exe` | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-75) |
| `0.9.4-76` | **Decompiler cleanup finished** — every split `$` inner-class file merged and all `varN` renamed tree-wide; update check moved to the **GitHub releases API** (+ interactive Help-menu check); Preferences dialog i18n/UX (localized `Core:` sections, translated/relabelled buttons, dependent checkboxes); configurable advanced-search formats | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-76) |
| `0.9.4-77` | Silent-failure fixes: installed-MSI Windows association now writes its `.reg` to a writable temp dir (+ warning on failure), failed screenshot save reports an error dialog | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.4-77) |
| `0.9.5` | **Plain incrementing version scheme** (drops the `0.9.4-<build>` suffix); fixes installers refusing to upgrade an existing install (the internal version was frozen at `0.9.4`, so the MSI ProductVersion never rose) | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.5) |
| `0.9.6` | **Windows associations: status + startup check** — the *Windows Registry* page shows each protocol/`.torrent` association's current state (Sancho / another app, user vs. machine); a startup check offers to create missing ones (or stop asking); toggle in Preferences | [Release](https://github.com/vsc55/sancho-p2p/releases/tag/0.9.6) |
