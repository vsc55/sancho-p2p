# Sancho — Technical Documentation

> Technical documentation for the **Sancho** project, produced by analysing the source code.
> Every statement is derived from the code; anything that **cannot** be deduced from the code is
> flagged explicitly as _"Not deducible from code"_.

This documentation is split into several documents:

| Document | Contents |
|---|---|
| **README.md** (this) | Project overview: goal, functionality, technologies, requirements |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Package structure, modules, design patterns, general and execution flow, improvement areas |
| [CLASSES.md](CLASSES.md) | Components by package and class, and the data model (entities, collections, wire DTOs, enumerations) |
| [API.md](API.md) | MLDonkey protocol (opcodes), command-line arguments and outbound HTTP calls. **There is no REST API** |
| [DEVELOPMENT.md](DEVELOPMENT.md) | Configuration, dependencies and how to build, run, test and extend |
| [HISTORY.md](HISTORY.md) | Version lineage and decompilation provenance |

Diagrams use [Mermaid](https://mermaid.js.org/), which GitHub renders natively in Markdown.

---

## 1. Project overview

### 1.1 Goal

Sancho is a **desktop GUI** for the **MLDonkey** P2P core. It is not a P2P client itself: it is a
front-end that connects to an already-running MLDonkey core (local or remote) through MLDonkey's
**binary GUI protocol** over TCP, and displays and controls downloads, searches, servers, chat
rooms, friends, shared files and statistics.

It is a **preservation / modernization** project: the source was recovered by **decompiling** the
last published binary (version `0.9.4-59`, with Vineflower) and ported to **JDK 17+** and modern
**SWT/JFace**, with bug fixes. Origin documented in `pom.xml:9-13` and in `../README.md`.

- _Not deducible from code:_ the actual on-screen appearance of each tab (the code only reveals the
  column/i18n-key wiring, not the rendered look).

### 1.2 Main functionality

| Area | Description | Tab / module |
|---|---|---|
| Transfers | Downloads (tree with per-chunk availability), their sources/clients, uploads, pending slot requests, comments, sub-files | `TransferTab` |
| Search | Simple / advanced / audio queries against the core; results per query | `SearchTab` |
| Servers | Known servers and users of the selected server | `ServerTab` |
| Shares | Shared / uploaded files | `SharesTab` |
| Statistics | Live bandwidth graphs, per-network statistics | `StatisticTab` |
| Console | MLDonkey text console (commands + scrollback) | `ConsoleTab` |
| Friends | Friend list, their browsed dirs/files, private messages | `FriendsTab` |
| Rooms | Chat rooms, users and per-room chat | `RoomsTab` |
| Web browser | Embedded browser with favorites and drag-to-download | `WebBrowserTab` |
| Completed downloads | History of finished downloads (dialog) | `downloadComplete/*` |

Cross-cutting features: core connection over **direct TCP or an SSH tunnel**; **optional local core
spawn**; **Windows associations** (`.torrent`, `ed2k:`, `magnet:`, `sig2dat:`); **sending links** to
a running core without opening the GUI (`sancho -l <link>`); **update check** against GitHub
Releases; **internationalization** in 15 languages; **system tray**.

### 1.3 Technologies used

| Technology | Version | Use |
|---|---|---|
| Java | **17+** (release 17; CI builds with JDK 21) | Language and runtime (`pom.xml:21`) |
| SWT | 3.124.0 | Native widgets (signed fragment per OS/arch) (`pom.xml:25`) |
| JFace | 3.31.0 **unsigned** | Viewers/preferences (local unsigned artifact; see Architecture) (`pom.xml:55-59`) |
| GNU Trove | 2.1.0 | Primitive collections (`TIntObjectHashMap`, …) (`pom.xml:61-65`) |
| JSch (mwiede fork) | 0.2.23 | SSH tunnel to the core (`pom.xml:68-73`) |
| Maven | — | Build; `maven-shade` for the uber-jar; per-OS/arch profiles |
| jpackage + WiX 3.14 | JDK | Native packaging (MSI/DEB/RPM/DMG/app-image) (`tools/build-app.ps1`) |
| GitHub Actions | — | CI (`build.yml`) and multi-platform releases (`release.yml`) |

Protocol: MLDonkey's **binary GUI protocol**, little-endian, up to version `41`
(`MLDonkeyCore.MAX_PROTOCOL = 41`).

### 1.4 Requirements

- **JRE/JDK 17 or newer** to run (17+ to build).
- A reachable **MLDonkey core** (local or remote) for the app to be useful; a local one can also be
  launched from the GUI if the `coreExecutable` preference is set (`Sancho.java:167-169`). With
  `-n`/`noCore` it starts without a core.
- For SSH to the core: SSH keys/credentials (uses `~/.ssh/known_hosts`, `id_dsa`/`id_rsa`).
- For Windows associations and the MSI: Windows; the MSI is built with WiX 3.14.
- _Not deducible from code:_ which specific MLDonkey core versions each protocol number maps to (the
  code only fixes `MAX_PROTOCOL = 41`).

---

For architecture, class, protocol and development detail, follow the links in the table above.
