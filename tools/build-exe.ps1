<#
.SYNOPSIS
    Builds a portable Windows app image (sancho.exe + bundled JRE) with jpackage.

.DESCRIPTION
    1. Compiles and packages the app (mvn clean package).
    2. Copies the app jar and its runtime dependencies into target\app-input.
    3. Runs jpackage to produce target\dist\sancho\sancho.exe with an embedded
       Java runtime, so it runs on machines without a JDK installed.

    The launcher is deliberately named sancho.exe (== VersionInfo.getName()) and
    started with -Duser.dir=$ROOTDIR. That is what makes the app's built-in
    "Preferences -> Windows registry" page work: it writes the association
    HKCR\.torrent -> "<dir>\sancho.exe" "-l" "%1" using System.getProperty("user.dir"),
    which $ROOTDIR pins to the folder that actually contains sancho.exe.

    After building, launch sancho.exe once, then in the GUI go to
    Preferences -> Windows registry -> File extensions -> Register association
    -> Update windows registry (may prompt for elevation, since regedit writes
    HKEY_CLASSES_ROOT). Double-clicking a .torrent then hands it to the running
    MLDonkey core via `sancho.exe -l "<file>"`.

.NOTES
    Requires: JDK 17+ (provides jpackage) and Maven on PATH.
    WiX Toolset is NOT required for this portable app image; it would only be
    needed to additionally produce an .msi/.exe *installer* (jpackage --type msi).
#>

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot   # repo root (tools\ is one level down)
Set-Location $root

$appVersion = "0.9.4"
$icon       = "packaging\windows\sancho.ico"

Write-Host "==> mvn clean package"
mvn -q clean package
if ($LASTEXITCODE -ne 0) { throw "Maven build failed" }

# Detect the produced application jar (branch-independent: -23 or -59).
$jar = Get-ChildItem "target\*.jar" |
    Where-Object { $_.Name -notmatch 'sources|javadoc' } |
    Select-Object -First 1
if (-not $jar) { throw "No application jar found in target\" }
$mainJar = $jar.Name
Write-Host "==> main jar: $mainJar"

Write-Host "==> collecting dependencies into target\app-input\lib"
mvn -q dependency:copy-dependencies "-DoutputDirectory=target\app-input\lib" "-DincludeScope=runtime"
if ($LASTEXITCODE -ne 0) { throw "dependency:copy-dependencies failed" }
Copy-Item "target\$mainJar" "target\app-input\" -Force

Write-Host "==> jpackage"
Remove-Item "target\dist" -Recurse -Force -ErrorAction SilentlyContinue
jpackage --type app-image --name sancho --app-version $appVersion `
    --input "target\app-input" --main-jar $mainJar --main-class "sancho.core.Sancho" `
    --icon $icon --dest "target\dist" `
    --vendor "Sancho preservation" --description "Sancho MLDonkey GUI" `
    --java-options "--enable-native-access=ALL-UNNAMED" `
    --java-options "-Djava.util.Arrays.useLegacyMergeSort=true" `
    --java-options "-Duser.dir=`$ROOTDIR"
if ($LASTEXITCODE -ne 0) { throw "jpackage failed" }

Write-Host ""
Write-Host "Done -> target\dist\sancho\sancho.exe"
