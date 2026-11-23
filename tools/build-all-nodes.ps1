# Builds every Stonecutter node ONE AT A TIME (sequential, single worker) so
# the heavy per-node setup pipelines (NeoForm, ForgeGradle Mavenizer, Loom)
# never overlap. Prints a per-node timing table at the end.
#
# Usage (from the repo root, with JDK 25 on PATH/JAVA_HOME):
#   powershell -File ..\tools\build-all-nodes.ps1
#   powershell -File ..\tools\build-all-nodes.ps1 -Nodes 26.3-nil,26.3-forge
param(
    [string[]]$Nodes = @(),
    [string]$GradleArgs = '--no-parallel --max-workers=1 --console=plain --stacktrace'
)

$ErrorActionPreference = 'Continue'
$repo = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $repo 'gradlew.bat'))) { $repo = (Get-Location).Path }

# Order: quickest/most-proven first, new toolchains early, the 9 heavy
# NeoForge decompiles last.
$defaultOrder = @(
    '26.3-fabric', '26.2-fabric', '26.1-26.1.2-fabric',
    '1.21.11-fabric', '1.21.9-1.21.10-fabric', '1.21.6-1.21.8-fabric',
    '1.21.5-fabric', '1.21.4-fabric', '1.21.2-1.21.3-fabric',
    '26.3-nil', '26.2-nil', '26.1-26.1.2-nil',
    '26.3-forge', '26.2-forge', '26.1-26.1.2-forge', '1.21.11-forge',
    '1.21.5-forge', '1.21.4-forge', '1.21.3-forge',
    '1.21.9-1.21.10-forge', '1.21.6-1.21.8-forge',
    '26.3-neoforge', '26.2-neoforge', '26.1-26.1.2-neoforge',
    '1.21.11-neoforge', '1.21.10-neoforge', '1.21.9-neoforge',
    '1.21.8-neoforge', '1.21.7-neoforge', '1.21.6-neoforge',
    '1.21.5-neoforge', '1.21.4-neoforge', '1.21.3-neoforge'
)
if ($Nodes.Count -gt 0) { $defaultOrder = $Nodes }

$results = @()
foreach ($node in $defaultOrder) {
    $log = Join-Path $repo "build-$node.log"
    Write-Host "=== $node ==="
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    Push-Location $repo
    try {
        & .\gradlew.bat (":${node}:build") @($GradleArgs.Split(' ')) *> $log
        $code = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    $sw.Stop()
    $status = if ($code -eq 0) { 'OK' } else { 'FAIL' }
    $results += [pscustomobject]@{ Node = $node; Status = $status; Seconds = [int]$sw.Elapsed.TotalSeconds }
    Write-Host ("    {0} in {1}s" -f $status, [int]$sw.Elapsed.TotalSeconds)
    if ($code -ne 0) {
        Select-String -Path $log -Pattern 'error:|What went wrong' -Context 0,3 | Select-Object -First 6 | ForEach-Object { Write-Host $_ }
    }
}

Write-Host ''
Write-Host '=== summary ==='
$results | Format-Table -AutoSize | Out-String | Write-Host
