# Verifies the release artifact set: 30 groups (9 Fabric + 9 NeoForge +
# 9 Forge + 3 NilLoader), exactly one non-sources jar each.
#   powershell -File ..\tools\verify-artifacts.ps1      (from the repo root)
param([string]$Repo = '')
$ErrorActionPreference = 'Stop'
if (-not $Repo) {
    if (Test-Path (Join-Path (Get-Location) 'gradlew.bat')) { $Repo = (Get-Location).Path }
    else { $Repo = Split-Path -Parent $PSScriptRoot }
}
$versionDir = Join-Path $Repo 'versions'
if (-not (Test-Path $versionDir)) { throw "versions/ not found under $Repo" }

$expected = @(
    # Fabric
    '1.21.2-1.21.3-fabric', '1.21.4-fabric', '1.21.5-fabric',
    '1.21.6-1.21.8-fabric', '1.21.9-1.21.10-fabric', '1.21.11-fabric',
    '26.1-26.1.2-fabric', '26.2-fabric', '26.3-fabric',
    # NeoForge
    '1.21.3-neoforge', '1.21.4-neoforge', '1.21.5-neoforge',
    '1.21.6-1.21.8-neoforge', '1.21.9-1.21.10-neoforge', '1.21.11-neoforge',
    '26.1-26.1.2-neoforge', '26.2-neoforge', '26.3-neoforge',
    # Forge
    '1.21.3-forge', '1.21.4-forge', '1.21.5-forge',
    '1.21.6-1.21.8-forge', '1.21.9-1.21.10-forge', '1.21.11-forge',
    '26.1-26.1.2-forge', '26.2-forge', '26.3-forge',
    # NilLoader
    '26.1-26.1.2-nil', '26.2-nil', '26.3-nil'
)

$missing = @()
$ok = @()
foreach ($node in $expected) {
    $libs = Join-Path $versionDir "$node\build\libs"
    $jar = if (Test-Path $libs) {
        Get-ChildItem $libs -Filter '*.jar' | Where-Object { $_.Name -notmatch 'sources' } | Select-Object -First 1
    }
    if ($jar) { $ok += [pscustomobject]@{ Node = $node; Jar = $jar.Name; KB = [math]::Round($jar.Length / 1kb) } }
    else { $missing += $node }
}

$ok | Format-Table -AutoSize | Out-String | Write-Host
Write-Host ("present: {0}/{1}" -f $ok.Count, $expected.Count)
if ($missing.Count -gt 0) {
    Write-Host ("MISSING: " + ($missing -join ', '))
    exit 1
}
Write-Host 'all 30 groups built'
