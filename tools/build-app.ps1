<#
.SYNOPSIS
    Builds a native package of Sancho with jpackage (cross-platform, pwsh 7+).

.DESCRIPTION
    Assembles the app jar + its runtime dependencies under target/app-input, then
    runs jpackage to produce a package of the requested -Type into target/dist:

      Windows : app-image (portable sancho.exe + bundled JRE)
      Linux   : app-image, or deb / rpm installers
      macOS   : app-image, or dmg / pkg

    The per-OS icon (packaging/windows|linux|macos) is selected automatically.
    The assembly step is skipped if target/app-input already exists, so the Linux
    release can produce both deb and rpm without rebuilding.

    The launcher is named `sancho` and started with -Duser.dir=$ROOTDIR so the
    app's built-in Windows-registry page derives the right executable path (see the
    torrent-association note in the README); harmless on Linux/macOS.

.PARAMETER Type
    jpackage type: app-image | deb | rpm | dmg | pkg. Defaults per OS
    (Windows/Linux -> app-image, macOS -> dmg).

.NOTES
    Requires JDK 17+ (jpackage) and Maven on PATH. On Linux, `deb` needs
    dpkg/fakeroot and `rpm` needs rpmbuild.
#>
param(
    [string]$Type = "",
    # jpackage bundle version (1-3 integers). Leave empty to use the real project
    # version from pom.xml (plain-incrementing 0.9.x), so the MSI/deb/rpm
    # ProductVersion rises every release and upgrades install cleanly over the
    # previous copy. macOS rejects a leading-zero bundle version, so the release
    # passes 1.0.0 there explicitly.
    [string]$AppVersion = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

# The project version from pom.xml (e.g. 0.9.5), used for the output file name and,
# by default, as the numeric package version below.
$fullVersion = $AppVersion
$pom = Join-Path $root "pom.xml"
if (Test-Path $pom) {
    $m = Select-String -Path $pom -Pattern '<version>([^<]+)</version>' | Select-Object -First 1
    if ($m) { $fullVersion = $m.Matches[0].Groups[1].Value }
}

# The numeric version jpackage stamps into the package (MSI ProductVersion / deb/rpm
# version / macOS CFBundleVersion). Windows Installer only performs a major upgrade
# when this strictly increases, so it must be the real, plain-incrementing project
# version (0.9.5, 0.9.6, ...) -- never a fixed number, or installing a newer build
# over an older one fails with "another version of this product is already installed"
# (error 1638). A caller may override it (macOS passes 1.0.0, which cannot have a
# leading-zero major field).
if (-not $AppVersion) { $AppVersion = $fullVersion }
$appVersion = $AppVersion

if (-not $Type) {
    if ($IsMacOS) { $Type = "dmg" } else { $Type = "app-image" }
}

if ($IsWindows)   { $icon = Join-Path $root "packaging/windows/sancho.ico" }
elseif ($IsMacOS) { $icon = Join-Path $root "packaging/macos/sancho.icns" }
else              { $icon = Join-Path $root "packaging/linux/sancho.png" }

$appInput = Join-Path $root "target/app-input"

if (-not (Test-Path $appInput)) {
    Write-Host "==> mvn clean package"
    mvn -q clean package
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed" }

    Write-Host "==> collecting dependencies into target/app-input/lib"
    mvn -q dependency:copy-dependencies "-DoutputDirectory=$appInput/lib" "-DincludeScope=runtime"
    if ($LASTEXITCODE -ne 0) { throw "dependency:copy-dependencies failed" }

    $jar = Get-ChildItem (Join-Path $root "target/*.jar") |
        Where-Object { $_.Name -notmatch 'sources|javadoc' } |
        Select-Object -First 1
    if (-not $jar) { throw "No application jar found in target/" }
    Copy-Item $jar.FullName $appInput
}

$mainJar = (Get-ChildItem (Join-Path $appInput "*.jar") | Select-Object -First 1).Name
Write-Host "==> jpackage --type $Type (main jar: $mainJar)"

$dest = Join-Path $root "target/dist"
New-Item -ItemType Directory -Force -Path $dest | Out-Null

$jpArgs = @(
    "--type", $Type, "--name", "sancho", "--app-version", $appVersion,
    "--input", $appInput, "--main-jar", $mainJar, "--main-class", "sancho.core.Sancho",
    "--icon", $icon, "--dest", $dest,
    "--vendor", "Sancho preservation", "--description", "Sancho MLDonkey GUI",
    "--java-options", "--enable-native-access=ALL-UNNAMED",
    "--java-options", "-Duser.dir=`$ROOTDIR"
)

# Windows installer (.msi / .exe): Start-menu + desktop shortcuts, a stable upgrade
# UUID (which, together with the build-number-bumped $appVersion above, lets a new
# release replace the installed one), and the custom WiX (packaging/windows/wix)
# that offers the optional .torrent/ed2k/magnet/sig2dat association registration.
# Per-machine install (default) -> associations land in HKLM. Requires JDK 25 +
# WiX 6 (the `wix` dotnet tool), which is what jpackage 25 drives.
$wixWork = Join-Path $root "target/wix-work"
$jpLog = Join-Path $root "target/jpackage-msi.log"
$isWindowsMsi = ($Type -eq "msi") -and ($IsWindows -or $env:OS -eq "Windows_NT")
if (($Type -eq "msi" -or $Type -eq "exe") -and ($IsWindows -or $env:OS -eq "Windows_NT")) {
    # --temp keeps jpackage's WiX intermediates (config/ + the app image) so the
    # multilingual post-step below can rebuild them; the dir must not pre-exist.
    if (Test-Path $wixWork) { Remove-Item -Recurse -Force $wixWork }
    $jpArgs += @(
        "--win-menu", "--win-menu-group", "Sancho",
        "--win-shortcut",
        "--win-shortcut-prompt",   # shows the options dialog carrying our REGISTERASSOC checkbox
        "--win-upgrade-uuid", "eb175abb-5d6d-4c3d-87f2-420da357de4f",
        "--resource-dir", (Join-Path $root "packaging/windows/wix"),
        "--temp", $wixWork
    )
    # wix-multilang.ps1 replays jpackage's own `wix build` per language, and recovers
    # that command line (and its working directory) from this verbose log.
    if ($isWindowsMsi) { $jpArgs += "--verbose" }
}

if ($isWindowsMsi) {
    # Verbose output is long; keep it in the log and only surface it if jpackage fails.
    jpackage @jpArgs *>&1 | Set-Content -Encoding UTF8 $jpLog
    if ($LASTEXITCODE -ne 0) {
        Get-Content $jpLog -Tail 40 | ForEach-Object { Write-Host $_ }
        throw "jpackage failed (full log: $jpLog)"
    }
} else {
    jpackage @jpArgs
    if ($LASTEXITCODE -ne 0) { throw "jpackage failed" }
}

# jpackage names the installer sancho-<appVersion>.<ext>; rename it to carry the full
# project version (with build number) so the local artifact is sancho-0.9.4-66.msi.
$installerMsi = Join-Path $dest "sancho-$appVersion.$Type"
if (($Type -eq "msi" -or $Type -eq "exe") -and ($fullVersion -ne $appVersion)) {
    if (Test-Path $installerMsi) {
        $installerMsi = Join-Path $dest "sancho-$fullVersion.$Type"
        Move-Item -Force (Join-Path $dest "sancho-$appVersion.$Type") $installerMsi
    }
}

# Fold every UI language into that single .msi (embedded transforms auto-applied by
# the OS language). Reuses jpackage's WiX intermediates left in target/wix-work.
if ($isWindowsMsi) {
    Write-Host "==> building multilingual installer"
    & (Join-Path $PSScriptRoot "wix-multilang.ps1") `
        -WorkDir $wixWork `
        -LangDir (Join-Path $root "packaging/windows/wix-lang") `
        -OutFile $installerMsi `
        -JpLog $jpLog
    if ($LASTEXITCODE) { throw "wix-multilang.ps1 failed" }
}

Write-Host ""
Write-Host "Done -> $dest"
Get-ChildItem $dest | ForEach-Object { Write-Host "  $($_.Name)" }
