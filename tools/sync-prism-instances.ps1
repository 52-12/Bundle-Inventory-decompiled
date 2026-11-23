# Copies every built group jar into its matching Prism Launcher instance
# ("BI <Loader> <mc label>"), replacing any older BundleInventory jar there.
# Quilt runs the Fabric jar, and NilLoader instances keep the agent jar at the
# instance's .minecraft root (the -javaagent path in instance.cfg is relative
# to the working directory). Prism may stay open; only the mods folders change.
#
#   powershell -File ..\tools\sync-prism-instances.ps1      (from the repo root)
param(
    [string]$Repo = '',
    [string]$PrismData = "$env:APPDATA\PrismLauncher"
)
$ErrorActionPreference = 'Stop'
if (-not $Repo) {
    if (Test-Path (Join-Path (Get-Location) 'gradlew.bat')) { $Repo = (Get-Location).Path }
    else { $Repo = Split-Path -Parent $PSScriptRoot }
}
# Prism may use a custom instance directory; prismlauncher.cfg is authoritative.
$instanceRoot = Join-Path $PrismData 'instances'
$cfgPath = Join-Path $PrismData 'prismlauncher.cfg'
if (Test-Path $cfgPath) {
    $line = Get-Content $cfgPath | Where-Object { $_ -match '^InstanceDir=' } | Select-Object -First 1
    if ($line) { $instanceRoot = ($line -replace '^InstanceDir=', '') }
}
$instancesDir = $instanceRoot

$pattern = '^BundleInventory-(?<loader>Fabric|NeoForge|Forge|Nil)-(?<version>.+)\+mc(?<mc>.+)\.jar$'
$copied = @()
foreach ($nodeDir in Get-ChildItem (Join-Path $Repo 'versions') -Directory) {
    $libs = Join-Path $nodeDir.FullName 'build\libs'
    if (-not (Test-Path $libs)) { continue }
    foreach ($jar in Get-ChildItem $libs -Filter '*.jar' | Where-Object { $_.Name -match $pattern -and $_.Name -notlike '*sources*' }) {
        $null = $jar.Name -match $pattern
        $loader = $Matches['loader']
        $mc = $Matches['mc']
        # Nilmods have no loader component, so their instances are named
        # "BI NilLoader <mc>".
        $instanceName = if ($loader -eq 'Nil') { "BI NilLoader $mc" } else { "BI $loader $mc" }
        $instance = Join-Path $instancesDir $instanceName
        if (-not (Test-Path $instance)) {
            Write-Warning ("no instance for {0} ({1})" -f $jar.Name, $instanceName)
            continue
        }
        # Nilmods are agents loaded from the working directory, not mods/.
        $destDir = if ($loader -eq 'Nil') { Join-Path $instance '.minecraft' } else { Join-Path $instance '.minecraft\mods' }
        New-Item -ItemType Directory -Force -Path $destDir | Out-Null
        Get-ChildItem $destDir -Filter 'BundleInventory-*.jar' | Remove-Item -Force
        Copy-Item $jar.FullName (Join-Path $destDir $jar.Name) -Force
        $copied += [pscustomobject]@{ Instance = $instanceName; Jar = $jar.Name }
    }
}
# Quilt check instance: Quilt Loader runs Fabric mods natively.
$quilt = Join-Path $instancesDir 'BI Quilt 26.3\.minecraft\mods'
if (Test-Path (Split-Path -Parent $quilt)) {
    $fabric263 = Get-ChildItem (Join-Path $Repo 'versions\26.3-fabric\build\libs') -Filter 'BundleInventory-Fabric-*.jar' |
        Where-Object { $_.Name -notlike '*sources*' } | Select-Object -First 1
    if ($fabric263) {
        New-Item -ItemType Directory -Force -Path $quilt | Out-Null
        Get-ChildItem $quilt -Filter 'BundleInventory-*.jar' | Remove-Item -Force
        Copy-Item $fabric263.FullName (Join-Path $quilt $fabric263.Name) -Force
        $copied += [pscustomobject]@{ Instance = 'BI Quilt 26.3'; Jar = $fabric263.Name }
    }
}

$copied | Sort-Object Instance | Format-Table -AutoSize | Out-String | Write-Host
Write-Output ("synced {0} jars into {1}" -f $copied.Count, $instancesDir)
