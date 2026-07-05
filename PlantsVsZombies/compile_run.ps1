# Auto-detect JavaFX SDK lib folder (including NetBeans bundled JavaFX) and compile/run the minimal JavaFX app
# Usage examples:
#   ./compile_run.ps1                                  # auto-detect
#   ./compile_run.ps1 -FxHome "C:\javafx-sdk-23.0.2"        # specify SDK root
#   ./compile_run.ps1 -FxLib  "D:\NetBeans-25\netbeans\javafx\lib"  # direct lib folder
#   ./compile_run.ps1 -Verbose                         # extra diagnostics via Write-Verbose

[CmdletBinding()]
param(
    [string]$FxHome,   # root of SDK (contains /lib)
    [string]$FxLib     # directly the lib folder (contains javafx-controls.jar)
)

$ErrorActionPreference = 'Stop'

# Build candidate list
$candidates = @()

# 1) Explicit parameters take priority
if ($FxLib)  { $candidates += $FxLib }
if ($FxHome) { $candidates += (Join-Path $FxHome 'lib') }

# 2) Environment variable
if ($env:JAVAFX_HOME) { $candidates += (Join-Path $env:JAVAFX_HOME 'lib') }

# 3) Common standalone SDK locations (21–23) on C: and D:, using globbing for versions like 23.0.2
foreach ($drive in 'C:','D:') {
    foreach ($pattern in 'javafx-sdk-21*','javafx-sdk-22*','javafx-sdk-23*','openjfx-sdk-21*','openjfx-sdk-22*','openjfx-sdk-23*') {
        try {
            Get-ChildItem -Path $drive\ -Filter $pattern -Directory -ErrorAction SilentlyContinue |
                ForEach-Object { $candidates += (Join-Path $_.FullName 'lib') }
        } catch {}
    }
}

# 4) NetBeans bundled JavaFX (if present)
foreach ($root in 'C:\NetBeans-25', 'D:\NetBeans-25', 'C:\Program Files\NetBeans-25', 'D:\Program Files\NetBeans-25') {
    $nbLib = Join-Path $root 'netbeans\javafx\lib'
    if (Test-Path $nbLib) { $candidates += $nbLib }
}

# Deduplicate + keep only existing
$candidates = $candidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -Unique

Write-Verbose "Candidates:"; $candidates | ForEach-Object { Write-Verbose "  $_" }

# Find a lib folder that contains a JavaFX controls jar (hyphen or dot variants)
$fx = $null
foreach ($c in $candidates) {
    if ((Test-Path (Join-Path $c 'javafx-controls.jar')) -or (Test-Path (Join-Path $c 'javafx.controls.jar'))) { $fx = $c; break }
    Write-Verbose "Skip (no controls jar): $c"
}

if (-not $fx) {
    Write-Host 'Could not find JavaFX lib folder.' -ForegroundColor Yellow
    Write-Host 'Provide -FxHome <sdkRoot> OR -FxLib <libFolder> or set JAVAFX_HOME.' -ForegroundColor Yellow
    Write-Host 'Example (SDK root): setx JAVAFX_HOME D:\javafx-sdk-23.0.2' -ForegroundColor Yellow
    Write-Host 'Example (direct lib): ./compile_run.ps1 -FxLib "D:\javafx-sdk-23.0.2\lib"' -ForegroundColor Yellow
    exit 1
}

Write-Host "Using JavaFX lib: $fx" -ForegroundColor Cyan
Write-Verbose "Jars:"
Get-ChildItem -Path $fx -Filter 'javafx-*.jar' | ForEach-Object { Write-Verbose "  $($_.Name)" }

# Compile every Java source under src (module-path for JavaFX)
$src = Get-ChildItem -Path 'src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
if (-not (Test-Path 'bin')) { New-Item -ItemType Directory -Path 'bin' | Out-Null }

& javac --module-path $fx --add-modules javafx.controls,javafx.media -d bin @src
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }


# Ensure images are on the runtime classpath (copy resources)
if (Test-Path 'src\pvz\images') {
    $dest = 'bin\pvz\images'
    if (-not (Test-Path $dest)) { New-Item -ItemType Directory -Path $dest | Out-Null }
    Copy-Item -Path 'src\pvz\images\*' -Destination $dest -Recurse -Force
}

# Ensure music is on the runtime classpath (copy resources)
if (Test-Path 'src\pvz\music') {
    $dest = 'bin\pvz\music'
    if (-not (Test-Path $dest)) { New-Item -ItemType Directory -Path $dest | Out-Null }
    Copy-Item -Path 'src\pvz\music\*' -Destination $dest -Recurse -Force
}

# Run
& java --module-path $fx --add-modules javafx.controls,javafx.graphics,javafx.media --enable-native-access=javafx.graphics,javafx.media -cp bin pvz.Main
