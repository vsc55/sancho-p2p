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
    # jpackage bundle version (1-3 integers). macOS rejects a leading 0, so the
    # release passes 1.0.0 there; the real version lives in the file name and the
    # app's own VersionInfo. Windows/Linux keep the accurate 0.9.4.
    [string]$AppVersion = "0.9.4"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

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
    "--java-options", "-Djava.util.Arrays.useLegacyMergeSort=true",
    "--java-options", "-Duser.dir=`$ROOTDIR"
)
jpackage @jpArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage failed" }

Write-Host ""
Write-Host "Done -> $dest"
Get-ChildItem $dest | ForEach-Object { Write-Host "  $($_.Name)" }
