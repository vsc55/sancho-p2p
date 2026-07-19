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
transfers, search, servers, friends, shares and a built-in web browser.

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

## 🏷 Version history

Sancho used a `0.9.4-NN` snapshot scheme, now plain-incrementing (`0.9.5`, `0.9.6`, …).
`main` always holds the newest modernized build.

- **Per-release changes:** [CHANGELOG.md](CHANGELOG.md) and the [Releases](../../releases) page
  (authoritative, up-to-date).
- **Version lineage & decompilation provenance:** [docs/HISTORY.md](docs/HISTORY.md) — note that
  `sancho-src/src` on `main` is **not** authentic source (recovered by decompiling the `0.9.4-59`
  binary); for genuine early source use the `0.9.4-23` tag.

## 📦 Contents

- `sancho-src/` — Java source tree (see the version note above)
- `docs/` — technical documentation (architecture, classes, protocol, development)
- `pom.xml` — Maven build
- `local-repo/` — signature-stripped JFace (see dependencies)
- `tools/` — `build-app.ps1` (native packages) and `unsign-libs.ps1`
- `packaging/` — per-OS app icons (windows/linux/macos)
- `.github/workflows/` — release automation
- `appimage/` — AppDir used to build the legacy Linux AppImage
- `CHANGELOG.md`, `ToDo.md`, `LICENSE`

## 📚 Documentation

Full technical documentation lives in **[docs/](docs/)** (rendered on GitHub, with Mermaid
diagrams):

| Document | Contents |
| --- | --- |
| [docs/README.md](docs/README.md) | Overview: goal, functionality, technologies, requirements |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Package structure, design patterns, general & execution flow, improvement areas |
| [docs/CLASSES.md](docs/CLASSES.md) | Components by package/class and the data model |
| [docs/API.md](docs/API.md) | MLDonkey protocol (opcodes), command-line arguments (no REST API) |
| [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) | Configuration, dependencies, and how to build/run/test/extend |
| [docs/HISTORY.md](docs/HISTORY.md) | Version lineage and decompilation provenance |

## 🔧 Building

Requires **JDK 17+** and Maven; the pom auto-selects the SWT fragment for your OS/arch
(verified in CI on Windows, Linux and macOS).

```bash
mvn clean package    # -> target/sancho-<version>.jar (+ the self-contained -all uber-jar)
mvn exec:java        # run
```

Native packages (MSI / DEB / RPM / DMG / app-image) are built with `jpackage`:

```powershell
pwsh tools/build-app.ps1 -Type msi      # or deb / rpm / dmg / app-image
```

Full build detail — VS Code F5 setup, the unsigned-JFace/`local-repo` strategy, native
packaging, silent MSI install, CI/CD and the legacy AppImage — is in
**[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)**.

## 🔗 File & protocol associations (Windows)

Sancho can register `.torrent` files and the `ed2k:` / `magnet:` / `sig2dat:` / `sfdl:`
protocols so double-clicking one hands it straight to the MLDonkey core
(`sancho.exe -l "%1"`, `S_DLLINK`) without opening the full GUI:

- **MSI installer:** a setup checkbox (on by default) registers them; skip it silently with
  `msiexec /i sancho-<ver>-win64.msi /qn REGISTERASSOC=0`.
- **Any build:** *Preferences → sancho: Windows registry* shows each association's current
  state (Sancho / another app, user vs. machine) and registers **per-user** (`HKCU`, no
  elevation) or, with *"Register for all users"*, machine-wide (`HKLM`, needs admin).
- On Windows, Sancho **checks associations at startup** and offers to create any that are
  missing (a checkbox turns the check off).

Silent-install options and the internals are in
[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) and [docs/API.md](docs/API.md#43-command-line-arguments).

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
SWT/JFace (EPL/CPL), GNU Trove (LGPL) and JSch (BSD-style). See
[LICENSE](LICENSE) and [sancho-src/CPL.txt](sancho-src/CPL.txt).
The upstream project's original changelog (2004–2006) is preserved at
[`appimage/usr/bin/distrib/ChangeLog`](appimage/usr/bin/distrib/ChangeLog).
