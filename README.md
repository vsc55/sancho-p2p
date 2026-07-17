# Sancho

**Sancho** is a Java/SWT desktop GUI for the **MLDonkey** peer-to-peer client
(eDonkey2000, Kademlia, BitTorrent and more), originally developed by
Rutger M. Ovidius in the mid-2000s. It monitors and controls an MLDonkey core:
transfers, search, servers, friends, shares, IRC and a built-in web browser.

This repository (`sancho-p2p`) **revives** it — the source has been recovered and
ported to build and run on a **current JDK (17+)** against modern SWT/JFace, with
reproducible Maven builds, a portable Windows executable, and automated releases.

## ⚙️ Status

- ✅ Builds and runs on **JDK 17+** (verified on Windows) against **SWT 3.124 / JFace 3.31**.
- ✅ Reproducible **Maven** build, **VS Code** F5 run config, portable Windows **`sancho.exe`** (jpackage), and a **GitHub Actions** release on tag.
- 📄 Changes are tracked in **[CHANGELOG.md](CHANGELOG.md)**; downloads are on the **[Releases](../../releases)** page.

It is **not affiliated** with the original authors' (inactive) project, and no
guarantees of compatibility, security or correctness are provided.

## 🏷 Versions

Sancho used a `0.9.4-NN` snapshot scheme.

| Version | What it is | Where |
| --- | --- | --- |
| `0.9.4-23` | Early **authentic** hand-written source | `0.9.4-23` tag — `git checkout 0.9.4-23` |
| `0.9.4-59` | Last **published** binary; its source was lost | recovered by decompilation |
| **`0.9.4-60`** | Current **modernized** build: the decompiled `-59` ported to modern SWT/JFace + JDK 17, plus bug fixes | **`main`** |

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
- `local-repo/` — signature-stripped SWT/JFace (see dependencies)
- `tools/` — `build-exe.ps1` (Windows `.exe`) and `unsign-libs.ps1`
- `packaging/windows/` — app icon
- `.github/workflows/` — release automation
- `appimage/` — AppDir used to build the legacy Linux AppImage
- `CHANGELOG.md`, `LICENSE`

## 🔧 Building

### Maven + VS Code

Verified to build and launch on **Windows with JDK 17+** (tested with Temurin 25).

```bash
mvn clean package        # compiles sancho-src/src, produces target/sancho-0.9.4-60.jar
mvn compile              # just compile
```

**Recommended VM args** (silence JDK native-access warnings; the legacy merge sort
is also requested in code, so a plain `java -jar` works without it):

```
--enable-native-access=ALL-UNNAMED
-Djava.util.Arrays.useLegacyMergeSort=true
```

#### In VS Code

1. Install the **Extension Pack for Java** (Red Hat).
2. Open the repository folder — the Java extension imports the Maven project and
   resolves dependencies automatically.
3. Press **F5** / use the **"Sancho (GUI)"** run configuration in
   [.vscode/launch.json](.vscode/launch.json).

#### Dependencies

Resolved from Maven Central: **Eclipse SWT + JFace**, **GNU Trove**
(`gnu.trove`, Trove 2.x flat package), **GNU RegExp**, **JSch**, and **PircBot**
(used by the IRC feature).

Sancho injects three of its own classes into the `org.eclipse.jface.viewers`
package (`ICustomViewer`, `CustomTableViewer`, `CustomTreeViewer`), and the JVM
refuses to load them into the otherwise **signed** JFace jar. So
signature-stripped copies of SWT and JFace are kept in [`local-repo/`](local-repo/)
under the `org.sancho.thirdparty` group id and consumed via a project-local Maven
repository. Regenerate them with [`tools/unsign-libs.ps1`](tools/unsign-libs.ps1).
For an OS other than Windows, strip signatures from the matching
`org.eclipse.swt.*` fragment and adjust `pom.xml`.

### Windows executable (jpackage)

A portable Windows app image — `sancho.exe` with a bundled Java runtime, so it runs
without a separate JDK — is produced by [tools/build-exe.ps1](tools/build-exe.ps1):

```powershell
pwsh tools/build-exe.ps1
# output: target/dist/sancho/sancho.exe   (folder is portable, copy it anywhere)
```

Requires JDK 17+ (for `jpackage`) and Maven on `PATH`. A WiX Toolset install is only
needed if you extend the script to emit an `.msi`/`.exe` installer (`--type msi`).

#### Torrent / protocol association (send to core on double-click)

Sancho has a built-in registry integration — no external `.reg` editing needed:

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

### Releases (GitHub Actions)

Pushing a version tag builds and publishes a Release with the jar and the Windows
app image, using the matching [CHANGELOG.md](CHANGELOG.md) section as the notes:

```bash
git tag 0.9.4-61 && git push origin 0.9.4-61
```

### AppImage (legacy)

An AppImage of the older build can be generated from the `appimage/` AppDir:

```bash
./appimagetool-x86_64.AppImage Sancho.AppDir/   # -> Sancho-x86_64.AppImage
```

## 🖥 Running

- **Windows:** `sancho.exe` (from a Release or `tools/build-exe.ps1`), or
  `java -jar target/sancho-0.9.4-60.jar`.
- **Other platforms / from source:** run `sancho.core.Sancho` with the SWT
  fragment for your OS on the classpath (see the dependencies note).

Sancho needs a reachable **MLDonkey core** to do anything useful; configure the
host/port and login in Preferences (or via the setup wizard on first run).

## 🧩 Original software & license

Sancho was originally developed by **Rutger M. Ovidius (2004–2005)** and
distributed under the **Common Public License (CPL)**. The original site is gone
but preserved via the Internet Archive:
<https://web.archive.org/web/20190304231314/http://sancho.awardspace.com/>

Bundled/used third-party components remain under their own licenses: Eclipse
SWT/JFace (EPL/CPL), GNU Trove (LGPL), GNU RegExp (LGPL), JSch (BSD-style),
PircBot. See [LICENSE](LICENSE) and [sancho-src/CPL.txt](sancho-src/CPL.txt).
The upstream project's original changelog (2004–2006) is preserved at
[`appimage/usr/bin/distrib/ChangeLog`](appimage/usr/bin/distrib/ChangeLog).
