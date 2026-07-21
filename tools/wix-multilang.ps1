<#
.SYNOPSIS
    Turns jpackage's single-language .msi into ONE multilingual installer.

.DESCRIPTION
    jpackage (with WiX 6) builds a single MSI in the base language (en). This script
    turns it into one MSI that carries every UI language:

      1. Re-run jpackage's own `wix build` once per culture -> one MSI per language.
      2. Diff each language MSI against the base -> a language transform (.mst).
      3. Embed every .mst into the base MSI as a sub-storage named by its LCID, and
         set the package "Template" summary language list.

    At install time Windows Installer applies the sub-storage transform whose LCID
    matches the machine's UI language (falling back to the base language), so a
    single sancho.msi shows German on a German Windows, Spanish on a Spanish one,
    and so on -- with no separate per-language downloads.

    Step 1 re-uses jpackage's *exact* `wix build` command line, recovered from its
    --verbose log, changing only -culture / -loc / -out. That deliberately avoids
    re-deriving jpackage's ~20 -d defines (product code, app size, OS version, ...):
    the product code in particular MUST match the base MSI or the transforms are
    invalid. Steps 2-3 use the WindowsInstaller COM API (Database.GenerateTransform
    and, in wix-embed-transforms.vbs, the _Storages sub-storage table), so they need
    no extra tooling -- WiX 4+ dropped the WiX 3 `torch` that used to do step 2.

.PARAMETER WorkDir
    The jpackage --temp directory (contains config/, image/, wixobj/).

.PARAMETER LangDir
    packaging/windows/wix-lang -- the extra-language MsiInstallerStrings_*.wxl
    (de/es/fr/it/ja/pt/zh). These are kept OUT of jpackage's --resource-dir on
    purpose: jpackage auto-discovers every MsiInstallerStrings_<culture>.wxl there
    and, on finding a language it does not bundle (Spanish), makes it the primary
    culture and mis-builds. The base English wxl does live in --resource-dir, so
    jpackage resolves the associations-checkbox string in its own build.

.PARAMETER OutFile
    The .msi produced by jpackage. It is used as the base language and is modified
    in place: the finished multilingual installer is written back to this path.

.PARAMETER JpLog
    jpackage's --verbose output, captured to a file by build-app.ps1. The `wix build`
    command line and its working directory are parsed out of it.

.NOTES
    Requires jpackage from JDK 25 and WiX 6 (`wix` dotnet tool) -- the same toolchain
    jpackage itself drives. Windows only.
#>
param(
    [Parameter(Mandatory = $true)][string]$WorkDir,
    [Parameter(Mandatory = $true)][string]$LangDir,
    [Parameter(Mandatory = $true)][string]$OutFile,
    [Parameter(Mandatory = $true)][string]$JpLog
)

$ErrorActionPreference = "Stop"

# Each language: the WiX culture used to build it (which also selects WixUIExtension's
# translation of the standard installer dialogs) and one or more decimal LCIDs to
# register the transform under. A translation can cover several LCIDs (e.g. Portuguese
# serves both Portugal 2070 and Brazil 1046). English (1033) is the base.
$BASE = [pscustomobject]@{ Key = "en"; Culture = "en-us"; Lcids = @(1033); Wxl = "MsiInstallerStrings_en.wxl" }
$EXTRA = @(
    [pscustomobject]@{ Key = "de"; Culture = "de-de"; Lcids = @(1031);       Wxl = "MsiInstallerStrings_de.wxl" }
    [pscustomobject]@{ Key = "fr"; Culture = "fr-fr"; Lcids = @(1036);       Wxl = "MsiInstallerStrings_fr.wxl" }
    [pscustomobject]@{ Key = "it"; Culture = "it-it"; Lcids = @(1040);       Wxl = "MsiInstallerStrings_it.wxl" }
    [pscustomobject]@{ Key = "ja"; Culture = "ja-jp"; Lcids = @(1041);       Wxl = "MsiInstallerStrings_ja.wxl" }
    [pscustomobject]@{ Key = "pt"; Culture = "pt-pt"; Lcids = @(2070, 1046); Wxl = "MsiInstallerStrings_pt.wxl" }
    [pscustomobject]@{ Key = "zh"; Culture = "zh-cn"; Lcids = @(2052);       Wxl = "MsiInstallerStrings_zh_CN.wxl" }
    [pscustomobject]@{ Key = "es"; Culture = "es-es"; Lcids = @(3082);       Wxl = "MsiInstallerStrings_es.wxl" }
)

# --- recover jpackage's own `wix build` invocation -------------------------------
if (-not (Test-Path $JpLog))   { throw "jpackage log not found: $JpLog" }
if (-not (Test-Path $OutFile)) { throw "base msi not found: $OutFile" }

$logLines = Get-Content $JpLog
$cmdLine = ($logLines | Where-Object { $_ -match '^\s*wix(\.exe)?\s+build\s' } | Select-Object -First 1)
$cwdLine = ($logLines | Where-Object { $_ -match 'Running wix(\.exe)? in\s' }  | Select-Object -First 1)
if (-not $cmdLine) { throw "could not find the 'wix build' command in $JpLog (was jpackage run with --verbose?)" }
if (-not $cwdLine) { throw "could not find wix's working directory in $JpLog" }
$buildCwd = ($cwdLine -replace '.*Running wix(\.exe)? in\s+', '').Trim()
if (-not (Test-Path $buildCwd)) { throw "wix working directory does not exist: $buildCwd" }

# Re-tokenize the logged command. jpackage logs it unquoted, so a -d define whose
# value contains spaces (JpAppDescription, JpAppVendor) is split across tokens: glue
# them back until the next switch or the first source file.
function ConvertTo-WixArgs([string]$line) {
    $tokens = ($line.Trim() -replace '^wix(\.exe)?\s+build\s+', '') -split '\s+'
    $result = New-Object System.Collections.Generic.List[string]
    $i = 0
    while ($i -lt $tokens.Count) {
        if ($tokens[$i] -eq '-d') {
            $value = $tokens[$i + 1]
            $j = $i + 2
            while ($j -lt $tokens.Count -and $tokens[$j] -notmatch '^-' -and $tokens[$j] -notmatch '\.(wxs|wxf|wxi)$') {
                $value += ' ' + $tokens[$j]; $j++
            }
            $result.Add('-d'); $result.Add($value); $i = $j
        } else {
            $result.Add($tokens[$i]); $i++
        }
    }
    return $result
}

$baseArgs = ConvertTo-WixArgs $cmdLine
if ($baseArgs -notcontains '-culture') { throw "parsed wix command has no -culture: $cmdLine" }
if ($baseArgs -notcontains '-out')     { throw "parsed wix command has no -out: $cmdLine" }

$config = Join-Path $WorkDir "config"
if (-not (Test-Path $config)) { throw "config dir not found under $WorkDir (did jpackage run with --temp?)" }

# Stage the extra-language wxl into config/ (overriding jpackage's token-less de/ja/zh
# and adding the languages it does not bundle at all).
foreach ($f in (Get-ChildItem -Path $LangDir -Filter "MsiInstallerStrings_*.wxl")) {
    Copy-Item -Force $f.FullName (Join-Path $config $f.Name)
}

$langOut = Join-Path $WorkDir "lang"
New-Item -ItemType Directory -Force -Path $langOut | Out-Null
# Cabinets are identical across languages (only strings differ), so cache and reuse
# them instead of recompressing ~50 MB for every culture.
$cabCache = Join-Path $WorkDir "cabcache"
New-Item -ItemType Directory -Force -Path $cabCache | Out-Null

# Rebuild the argument list for one language: drop every -loc, point -culture at this
# language, -out at its own MSI, and reuse the cabinet cache.
function Get-LangArgs([pscustomobject]$lang, [string]$outMsi) {
    $out = New-Object System.Collections.Generic.List[string]
    $i = 0
    while ($i -lt $baseArgs.Count) {
        switch ($baseArgs[$i]) {
            '-loc'     { $i += 2; continue }                                        # replaced below
            '-culture' { $out.Add('-culture'); $out.Add($lang.Culture); $i += 2; continue }
            '-out'     { $out.Add('-out');     $out.Add($outMsi);       $i += 2; continue }
            default    { $out.Add($baseArgs[$i]); $i++ }
        }
    }
    $out.Add('-loc'); $out.Add((Join-Path $config $lang.Wxl))
    $out.Add('-cabcache'); $out.Add($cabCache)
    return $out
}

# --- 1) one MSI per extra language ----------------------------------------------
# The base language is jpackage's own output ($OutFile) -- same product code, same
# everything, so the diffs below contain nothing but localized strings.
Write-Host "==> multilang: building $($EXTRA.Count) languages (base $($BASE.Culture) = jpackage's output)"

$transforms = @()   # @{ Lcid; Mst } -- one entry per LCID (a language may have several)
$installer = New-Object -ComObject WindowsInstaller.Installer
foreach ($lang in $EXTRA) {
    $loc = Join-Path $config $lang.Wxl
    if (-not (Test-Path $loc)) { throw "missing localization: $loc" }
    $langMsi = Join-Path $langOut "sancho-$($lang.Key).msi"
    $wixArgs = Get-LangArgs $lang $langMsi

    Push-Location $buildCwd
    try {
        & wix.exe build @wixArgs
        if ($LASTEXITCODE -ne 0) { throw "wix build failed for $($lang.Key) (exit $LASTEXITCODE)" }
    } finally { Pop-Location }

    # 2) language transform: applying it to the base MSI yields the language MSI.
    #    (WiX 4+ has no `torch`; Database.GenerateTransform is the COM equivalent.)
    $mst = Join-Path $langOut "$($lang.Lcids[0]).mst"
    Remove-Item $mst -Force -ErrorAction SilentlyContinue
    $dbLang = $installer.OpenDatabase($langMsi, 0)
    $dbBase = $installer.OpenDatabase($OutFile, 0)
    $null = $dbLang.GenerateTransform($dbBase, $mst)
    $dbLang.CreateTransformSummaryInfo($dbBase, $mst, 0, 0)
    # Release both handles right away: a lingering read-only handle on the base MSI
    # keeps the file locked, and the embedding step below has to open it for writing.
    [void][Runtime.InteropServices.Marshal]::ReleaseComObject($dbBase)
    [void][Runtime.InteropServices.Marshal]::ReleaseComObject($dbLang)
    if (-not (Test-Path $mst)) { throw "no transform produced for $($lang.Key) (is it identical to the base?)" }

    foreach ($lcid in $lang.Lcids) { $transforms += [pscustomobject]@{ Lcid = $lcid; Mst = $mst } }
    Write-Host "    + $($lang.Culture) -> $($lang.Lcids -join '/')"
}

# --- 3) embed transforms + set the language list --------------------------------
# One sub-storage per transform (named by decimal LCID) plus the Package Template
# language list. Done in wix-embed-transforms.vbs, because MSI's indexed record
# properties (Record.StringData(i) = x) cannot be set through PowerShell's COM
# binder; cscript is always present, so this needs no extra tooling either.
$langList = ($BASE.Lcids + ($EXTRA | ForEach-Object { $_.Lcids })) -join ','
$vbs = Join-Path $PSScriptRoot "wix-embed-transforms.vbs"
$vbsArgs = @($OutFile, $langList)
foreach ($t in $transforms) { $vbsArgs += @("$($t.Lcid)", $t.Mst) }

# Drop our own handle on the base MSI first, or the embedding cannot open it for writing.
[void][Runtime.InteropServices.Marshal]::ReleaseComObject($installer)
[GC]::Collect(); [GC]::WaitForPendingFinalizers()

& cscript.exe //nologo $vbs @vbsArgs
if ($LASTEXITCODE -ne 0) { throw "wix-embed-transforms.vbs failed (exit $LASTEXITCODE)" }

# Verify what actually landed in the file. A WSH runtime error does not necessarily
# set a non-zero exit code, so trusting the exit status alone can ship a silently
# single-language installer; check the result instead of assuming it.
$check = New-Object -ComObject WindowsInstaller.Installer
$template = $check.SummaryInformation($OutFile, 0).Property(7)
$embedded = $check.OpenDatabase($OutFile, 0).OpenView("SELECT ``Name`` FROM _Storages")
$embedded.Execute()
$count = 0
while ($embedded.Fetch()) { $count++ }
$got = ($template -split ';')[-1]
if ($count -ne $transforms.Count) {
    throw "embedding failed: expected $($transforms.Count) language transforms in $OutFile, found $count"
}
if ($got -ne $langList) {
    throw "embedding failed: package language list is '$got', expected '$langList'"
}

Write-Host "==> multilang: $OutFile  ($count transforms, languages: $langList)"
