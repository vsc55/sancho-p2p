' Embed language transforms into an MSI as sub-storages and set the package
' language list, so one .msi carries every UI language (Windows Installer applies
' the sub-storage transform matching the machine's UI language at install time).
'
' Does exactly what the Windows SDK's wisubstg.vbs + WiLangId.vbs do, but bundled
' with Sancho so no SDK sample scripts need to be present on the build/CI machine.
' Called by tools/wix-multilang.ps1.
'
' Usage:
'   cscript //nologo wix-embed-transforms.vbs <msi> <langList> <lcid> <mst> [<lcid> <mst> ...]
'     <msi>      target .msi (modified in place)
'     <langList> comma-separated LCID list for the package Template, base first
'                (e.g. "1033,1031,1041,2052,3082")
'     <lcid> <mst> pairs: sub-storage name (decimal LCID) and its transform file

Option Explicit

Const msiOpenDatabaseModeTransact = 1
Const msiViewModifyAssign = 3            ' insert, or replace a row with the same key
Const PID_TEMPLATE = 7

Dim installer, db, view, rec, si, template, platform
Dim msi, langList, i, lcid, mst, count

If WScript.Arguments.Count < 4 Then
    WScript.Echo "error: need <msi> <langList> then <lcid> <mst> pairs"
    WScript.Quit 2
End If

msi = WScript.Arguments(0)
langList = WScript.Arguments(1)

' A WSH runtime error alone does not reliably set a non-zero exit code, so every MSI
' call is checked explicitly: a half-embedded installer must fail the build loudly
' rather than ship as a silently single-language one.
On Error Resume Next

Set installer = CreateObject("WindowsInstaller.Installer")
Fail "creating the WindowsInstaller object"

' Opening for write fails if anything still holds a handle on the .msi.
Set db = installer.OpenDatabase(msi, msiOpenDatabaseModeTransact)
Fail "opening " & msi & " for writing"

count = 0
For i = 2 To WScript.Arguments.Count - 1 Step 2
    lcid = WScript.Arguments(i)
    mst = WScript.Arguments(i + 1)
    Set view = db.OpenView("SELECT `Name`,`Data` FROM _Storages")
    Set rec = installer.CreateRecord(2)
    rec.StringData(1) = lcid                ' sub-storage name = decimal LCID
    rec.SetStream 2, mst                    ' the transform bytes
    view.Modify msiViewModifyAssign, rec
    view.Close
    Fail "embedding transform " & mst & " as " & lcid
    count = count + 1
Next

' Template summary property: "<platform>;<lcid0>,<lcid1>,..." (base LCID first).
Set si = db.SummaryInformation(3)
template = si.Property(PID_TEMPLATE)
platform = Split(template, ";")(0)
si.Property(PID_TEMPLATE) = platform & ";" & langList
si.Persist
Fail "setting the package language list"
db.Commit
Fail "committing " & msi

WScript.Echo "embedded " & count & " transform(s); Template=" & platform & ";" & langList

' Aborts with a non-zero exit code if the preceding call raised an error, naming the
' step that failed. Clears Err so each checkpoint reports only its own failure.
Sub Fail(context)
    If Err.Number <> 0 Then
        WScript.Echo "error: " & context & ": [0x" & Hex(Err.Number) & "] " & Err.Description
        WScript.Quit 1
    End If
    Err.Clear
End Sub
