# Sancho

[![Build](https://github.com/vsc55/sancho-p2p/actions/workflows/build.yml/badge.svg)](https://github.com/vsc55/sancho-p2p/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/vsc55/sancho-p2p?display_name=tag&sort=date)](https://github.com/vsc55/sancho-p2p/releases)
[![Downloads](https://img.shields.io/github/downloads/vsc55/sancho-p2p/total)](https://github.com/vsc55/sancho-p2p/releases)
[![License](https://img.shields.io/badge/license-CPL--1.0-blue)](LICENSE)
![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)
![Platforms](https://img.shields.io/badge/platform-Windows%20%7C%20Linux%20%7C%20macOS-informational)

**Sancho** is a Java/SWT desktop GUI for the **MLDonkey** peer-to-peer client
(eDonkey2000, Kademlia, BitTorrent and more), originally developed by
Rutger M. Ovidius in the mid-2000s. It monitors and controls an MLDonkey core:
transfers, search, servers, friends, shares, IRC and a built-in web browser.

This repository (`sancho-p2p`) **revives** it — the source has been recovered and
ported to build and run on a **current JDK (17+)** against modern SWT/JFace, with
reproducible Maven builds, native packages for **Windows, Linux and macOS**, and
automated releases.

## ⚙️ Status

- ✅ Builds and runs on **JDK 17+** against **SWT 3.124 / JFace 3.31**; CI builds on
  **Linux, Windows and macOS**.
- ✅ Every tag publishes native packages via `jpackage` — Windows portable `.zip`,
  Linux `.deb` + `.rpm`, macOS `.dmg` (plus a plain jar) — with **VS Code** F5 run
  config and a reproducible **Maven** build.
- 📄 Changes are tracked in **[CHANGELOG.md](CHANGELOG.md)**; downloads are on the
  **[Releases](../../releases)** page.

It is **not affiliated** with the original authors' (inactive) project, and no
guarantees of compatibility, security or correctness are provided.

## ⬇️ Download & install

Grab the latest build from the **[Releases](../../releases)** page:

| Platform | File | Install / run |
| --- | --- | --- |
| Windows (installer) | `sancho-<ver>-win64.msi` | Run it — installs to Program Files with Start-menu/desktop shortcuts and (optional) `.torrent`/`ed2k`/`magnet`/`sig2dat` associations |
| Windows (portable) | `sancho-<ver>-win64.zip` | Unzip anywhere, run `sancho.exe` (portable, bundled JRE) |
| Debian / Ubuntu | `sancho-<ver>-linux-x86_64.deb` | `sudo apt install ./sancho-*.deb` |
| Fedora / RHEL | `sancho-<ver>-linux-x86_64.rpm` | `sudo dnf install ./sancho-*.rpm` |
| macOS (Apple Silicon) | `sancho-<ver>-macos-arm64.dmg` | Open the `.dmg`, drag **Sancho** to Applications |
| With your own JRE | `sancho-<ver>-<platform>.jar` | `java -jar sancho-<ver>-<platform>.jar` — self-contained, one file (pick your platform) |

The native packages bundle their own Java runtime; the `sancho-<ver>-<platform>.jar`
is a self-contained uber-jar (all dependencies inside) that needs only an installed
JRE. The macOS `.dmg` is **unsigned**, so Gatekeeper will warn — right-click →
**Open** the first time (or `xattr -dr com.apple.quarantine /Applications/sancho.app`).
Sancho needs a reachable **MLDonkey core** — set the host/port and login in
Preferences (or the first-run setup wizard).

## 🏷 Versions

Sancho used a `0.9.4-NN` snapshot scheme.

| Version | What it is | Where |
| --- | --- | --- |
| `0.9.4-23` | Early **authentic** hand-written source | [`0.9.4-23`](../../releases/tag/0.9.4-23) tag — `git checkout 0.9.4-23` |
| `0.9.4-59` | Last **published** binary (source lost); recovered by decompilation | [`0.9.4-59`](../../releases/tag/0.9.4-59) tag / `decompiled-0.9.4-59` branch |
| `0.9.4-60` | First **modernized** build: decompiled `-59` ported to modern SWT/JFace + JDK 17, plus bug fixes | [Release](../../releases/tag/0.9.4-60) |
| `0.9.4-61` | Maintained **JSch** fork + CI build check | [Release](../../releases/tag/0.9.4-61) |
| `0.9.4-62` | **Cross-platform** release artifacts (Windows / Linux / macOS) | [Release](../../releases/tag/0.9.4-62) |
| `0.9.4-63` | Self-contained **uber-jar** (one-file `java -jar`) | [Release](../../releases/tag/0.9.4-63) |
| `0.9.4-64` | Dropped `gnu-regexp` for the JDK's `java.util.regex` | [Release](../../releases/tag/0.9.4-64) |
| `0.9.4-65` | Release pipeline off deprecated Node-20 artifact actions | [Release](../../releases/tag/0.9.4-65) |
| `0.9.4-66` | macOS external-link fix, platform-code cleanup, Dependabot | [Release](../../releases/tag/0.9.4-66) |
| `0.9.4-67` | Windows **`.msi` installer** (optional association prompt + silent-install support) | [Release](../../releases/tag/0.9.4-67) |
| `0.9.4-68` | Cleaner build output (shade + checksum warnings), serialized release runs | [Release](../../releases/tag/0.9.4-68) |
| `0.9.4-69` | Startup toolbar fix, real build version in the title, Edge web browser on Windows | [Release](../../releases/tag/0.9.4-69) |
| `0.9.4-70` | Latent-bug sweep (core spawn, Linux links, sort contracts, leaks), local stable sort, pure `java.util.regex` | [Release](../../releases/tag/0.9.4-70) |
| `0.9.4-71` | Virtual-table render fix (stale/duplicate/stuck rows), IRC nick + connect-dialog fixes | [Release](../../releases/tag/0.9.4-71) |

**`main`** always holds the newest modernized build; [CHANGELOG.md](CHANGELOG.md)
and the [Releases](../../releases) page are the authoritative, up-to-date list.

> `sancho-src/src` on `main` is **not** authentic source. It was recovered by
> decompiling the last published `0.9.4-59` binary (Vineflower) and then ported to
> modern SWT/JFace. It includes features that never had preserved source (IRC, web
> browser, network stats, file comments). For the genuine early source, use the
> `0.9.4-23` tag. Reconstruction caveats: the GCJ-only `Prov` bootstrap is a no-op
> stub, `MyMenuManager` / dynamic field-editor clearing use reflection over JFace
> internals, and the removed SWT Mozilla backend is mapped to Edge on Windows.

## 📦 Contents

- `sancho-src/` — Java source tree (see the version note above)
- `pom.xml` — Maven build
- `local-repo/` — signature-stripped JFace (see dependencies)
- `tools/` — `build-app.ps1` (native packages) and `unsign-libs.ps1`
- `packaging/` — per-OS app icons (windows/linux/macos)
- `.github/workflows/` — release automation
- `appimage/` — AppDir used to build the legacy Linux AppImage
- `CHANGELOG.md`, `ToDo.md`, `LICENSE`

## 🔧 Building

### Maven + VS Code

Builds on **Windows, Linux and macOS** with **JDK 17+** — the pom auto-selects the
right SWT fragment for your OS/arch (verified in CI on all three). Verified to
launch on Windows (Temurin 25).

```bash
mvn clean package        # compiles sancho-src/src, produces target/sancho-<version>.jar
mvn compile              # just compile
```

**Recommended VM args** (silence JDK native-access warnings):

```text
--enable-native-access=ALL-UNNAMED
```

#### In VS Code

1. Install the **Extension Pack for Java** (Red Hat).
2. Open the repository folder — the Java extension imports the Maven project and
   resolves dependencies automatically.
3. Press **F5** / use the **"Sancho (GUI)"** run configuration in
   [.vscode/launch.json](.vscode/launch.json).

#### Dependencies

Resolved from Maven Central: **Eclipse SWT + JFace**, **GNU Trove**
(`gnu.trove`, Trove 2.x flat package), **JSch**, and **PircBot** (used by the IRC
feature). The abandoned **GNU RegExp** dependency was dropped — regexes now run on
the JDK's `java.util.regex` behind a thin `sancho.utility.regex` adapter.

**SWT is platform-specific**, so the pom auto-selects the fragment for the build
machine via OS/arch profiles (`win32`, `gtk.linux.x86_64/aarch64`,
`cocoa.macosx.x86_64/aarch64`); override with
`-Dswt.artifactId=org.eclipse.swt.<ws>.<os>.<arch>` if needed.

Sancho injects three of its own classes into the `org.eclipse.jface.viewers`
package (`ICustomViewer`, `CustomTableViewer`, `CustomTreeViewer`), and the JVM
refuses to load them into the otherwise **signed** JFace jar. So a single,
platform-independent **signature-stripped JFace** is kept in
[`local-repo/`](local-repo/) under the `org.sancho.thirdparty` group id and
consumed via a project-local Maven repository (with `.sha1`/`.md5` checksums so the
build validates cleanly; regenerate with
[`tools/unsign-libs.ps1`](tools/unsign-libs.ps1)). SWT itself is used unmodified
(stock, signed) — nothing is injected into its packages.

### Native packages (jpackage)

[tools/build-app.ps1](tools/build-app.ps1) bundles a Java runtime with the app via
`jpackage` — no separate JDK needed to run it. It is cross-platform (pwsh 7+):

```powershell
pwsh tools/build-app.ps1                # default: app-image (Windows/Linux) or dmg (macOS)
pwsh tools/build-app.ps1 -Type deb      # or rpm / dmg / app-image
# output under target/dist/
```

Requires JDK 17+ (for `jpackage`) and Maven on `PATH`. On Linux, `deb` needs
dpkg/fakeroot and `rpm` needs rpmbuild. Releases build these automatically per OS
(Windows portable `.zip`, Linux `.deb` + `.rpm`, macOS `.dmg`).

#### Torrent / protocol association (send to core on double-click)

**With the `.msi` installer** this is offered up front: the setup dialog has a
checkbox *"Register .torrent files and ed2k:, magnet:, sig2dat: links…"* (on by
default). For a silent/unattended install, control it with the public property:

```powershell
msiexec /i sancho-<ver>-win64.msi /qn REGISTERASSOC=0   # 0 = do not register
```

The installer writes the associations to `HKLM\Software\Classes` (per-machine) with
the command `"…\sancho.exe" "-l" "%1"` — the same layout the app itself uses.

**Any install (portable zip too)** can also do it from inside the app — no external
`.reg` editing needed:

1. Run `sancho.exe` and open **Preferences → sancho: Windows registry**.
2. **File extensions** tab → **Register association** → **Update windows registry**
   associates `.torrent` files. The **Protocols** tab does the same for `ed2k:`,
   `magnet:` and `sig2dat:`. (This runs `regedit`, so Windows may prompt for elevation.)

Double-clicking a `.torrent` (or an `ed2k:`/`magnet:` link) then runs
`sancho.exe -l "<file-or-link>"`, which connects to the MLDonkey core and hands it the
download directly (`S_DLLINK`), without opening the full GUI. As the preference page
warns, for `.torrent` files the core should be able to read the file (same machine, or
mldonkey core > 2.5.17).

The launcher is named `sancho.exe` and started with `-Duser.dir=$ROOTDIR` on purpose:
the registry page derives the executable path from `user.dir`, and `$ROOTDIR` pins it to
the folder that actually contains `sancho.exe`, so the written association is always correct.

#### Unattended / silent install (MSI)

The `.msi` is a standard Windows Installer package, so it installs silently with
`msiexec`:

```powershell
# silent per-machine install (associations registered by default)
msiexec /i sancho-<ver>-win64.msi /qn

# silent install WITHOUT registering the .torrent/ed2k/magnet/sig2dat associations
msiexec /i sancho-<ver>-win64.msi /qn REGISTERASSOC=0

# choose the install directory, and write a verbose log
msiexec /i sancho-<ver>-win64.msi /qn INSTALLDIR="C:\Apps\Sancho" /l*v install.log

# silent uninstall
msiexec /x sancho-<ver>-win64.msi /qn
```

Notes:

- **`REGISTERASSOC`** (public property) — `1` (default) registers the associations,
  `0` skips them. In an interactive install it maps to the setup dialog checkbox.
- The install is **per-machine** (writes `HKLM`), so a silent install needs an
  elevated/admin context.
- `msiexec` flags: `/qn` no UI, `/qb` basic UI, `/norestart`, `/l*v <file>` full log.

### Releases (GitHub Actions)

Pushing a version tag builds on a Windows/Linux/macOS matrix and publishes one
Release with all artifacts — Windows portable `.zip` + jar, Linux `.deb` + `.rpm`,
macOS `.dmg` — using the matching [CHANGELOG.md](CHANGELOG.md) section as the notes:

```bash
git tag 0.9.4-63 && git push origin 0.9.4-63
```

### AppImage (legacy)

An AppImage of the older build can be generated from the `appimage/` AppDir:

```bash
./appimagetool-x86_64.AppImage Sancho.AppDir/   # -> Sancho-x86_64.AppImage
```

## 🖥 Running

- **Windows:** `sancho.exe` (from a Release or `tools/build-app.ps1`), or
  `java -jar target/sancho-<version>.jar`.
- **Linux / macOS:** `mvn clean package` picks your platform's SWT automatically;
  run with `mvn exec:java` or `java -cp target/classes:<deps> sancho.core.Sancho`.

Sancho needs a reachable **MLDonkey core** to do anything useful; configure the
host/port and login in Preferences (or via the setup wizard on first run).

## 🧩 Original software & license

Sancho was originally developed by **Rutger M. Ovidius (2004–2005)** and
distributed under the **Common Public License (CPL)**. The original site is gone
but preserved via the Internet Archive:
<https://web.archive.org/web/20190304231314/http://sancho.awardspace.com/>

Bundled/used third-party components remain under their own licenses: Eclipse
SWT/JFace (EPL/CPL), GNU Trove (LGPL), JSch (BSD-style), PircBot. See
[LICENSE](LICENSE) and [sancho-src/CPL.txt](sancho-src/CPL.txt).
The upstream project's original changelog (2004–2006) is preserved at
[`appimage/usr/bin/distrib/ChangeLog`](appimage/usr/bin/distrib/ChangeLog).
