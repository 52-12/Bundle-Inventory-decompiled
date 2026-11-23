# Creates one Prism Launcher instance per release group (30) plus a Quilt
# check instance, each with the built mod jar installed. Prism must be closed.
#
#   powershell -File ..\tools\create-prism-instances.ps1      (from the repo root)
param(
    [string]$Repo = '',
    [string]$PrismData = "$env:APPDATA\PrismLauncher",
    [string]$Java21 = 'C:/Program Files/Eclipse Adoptium/jdk-21.0.9.10-hotspot/bin/javaw.exe',
    [string]$Java25 = 'C:/Program Files/Eclipse Adoptium/jdk-25.0.1.8-hotspot/bin/javaw.exe'
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
if (-not (Test-Path $instancesDir)) { New-Item -ItemType Directory -Force -Path $instancesDir | Out-Null }

# group => minecraft version, loader uid+version (or $null for nilmods)
$fabricLoader = '0.20.1'
$groups = @(
    @{ node = '1.21.2-1.21.3-fabric';   mc = '1.21.2'; loader = 'net.fabricmc.fabric-loader'; lv = $fabricLoader },
    @{ node = '1.21.4-fabric';          mc = '1.21.4'; loader = 'net.fabricmc.fabric-loader'; lv = $fabricLoader },
    @{ node = '1.21.5-fabric';          mc = '1.21.5'; loader = 'net.fabricmc.fabric-loader'; lv = $fabricLoader },
    @{ node = '1.21.6-1.21.8-fabric';   mc = '1.21.6'; loader = 'net.fabricmc.fabric-loader'; lv = $fabricLoader },
    @{ node = '1.21.9-1.21.10-fabric';  mc = '1.21.9'; loader = 'net.fabricmc.fabric-loader'; lv = $fabricLoader },
    @{ node = '1.21.11-fabric';         mc = '1.21.11'; loader = 'net.fabricmc.fabric-loader'; lv = $fabricLoader },
    @{ node = '26.1-26.1.2-fabric';     mc = '26.1'; loader = 'net.fabricmc.fabric-loader'; lv = $fabricLoader },
    @{ node = '26.2-fabric';            mc = '26.2'; loader = 'net.fabricmc.fabric-loader'; lv = $fabricLoader },
    @{ node = '26.3-fabric';            mc = '26.3'; loader = 'net.fabricmc.fabric-loader'; lv = $fabricLoader },

    @{ node = '1.21.3-neoforge';        mc = '1.21.3'; loader = 'net.neoforged'; lv = '21.3.100' },
    @{ node = '1.21.4-neoforge';        mc = '1.21.4'; loader = 'net.neoforged'; lv = '21.4.161' },
    @{ node = '1.21.5-neoforge';        mc = '1.21.5'; loader = 'net.neoforged'; lv = '21.5.101' },
    @{ node = '1.21.6-1.21.8-neoforge'; mc = '1.21.6'; loader = 'net.neoforged'; lv = '21.6.20-beta' },
    @{ node = '1.21.9-1.21.10-neoforge';mc = '1.21.9'; loader = 'net.neoforged'; lv = '21.9.16-beta' },
    @{ node = '1.21.11-neoforge';       mc = '1.21.11'; loader = 'net.neoforged'; lv = '21.11.48' },
    @{ node = '26.1-26.1.2-neoforge';   mc = '26.1'; loader = 'net.neoforged'; lv = '26.1.0.19-beta' },
    @{ node = '26.2-neoforge';          mc = '26.2'; loader = 'net.neoforged'; lv = '26.2.0.95' },
    @{ node = '26.3-neoforge';          mc = '26.3'; loader = 'net.neoforged'; lv = '26.3.0.115' },

    @{ node = '1.21.3-forge';           mc = '1.21.3'; loader = 'net.minecraftforge'; lv = '1.21.3-53.1.16' },
    @{ node = '1.21.4-forge';           mc = '1.21.4'; loader = 'net.minecraftforge'; lv = '1.21.4-54.1.22' },
    @{ node = '1.21.5-forge';           mc = '1.21.5'; loader = 'net.minecraftforge'; lv = '1.21.5-55.1.18' },
    @{ node = '1.21.6-1.21.8-forge';    mc = '1.21.6'; loader = 'net.minecraftforge'; lv = '1.21.6-56.0.9' },
    @{ node = '1.21.9-1.21.10-forge';   mc = '1.21.9'; loader = 'net.minecraftforge'; lv = '1.21.9-59.0.5' },
    @{ node = '1.21.11-forge';          mc = '1.21.11'; loader = 'net.minecraftforge'; lv = '1.21.11-61.2.4' },
    @{ node = '26.1-26.1.2-forge';      mc = '26.1'; loader = 'net.minecraftforge'; lv = '26.1-62.0.9' },
    @{ node = '26.2-forge';             mc = '26.2'; loader = 'net.minecraftforge'; lv = '26.2-65.1.5' },
    @{ node = '26.3-forge';             mc = '26.3'; loader = 'net.minecraftforge'; lv = '26.3-66.1.9' },

    # NilLoader: stock Minecraft + the standalone jar as a java agent.
    @{ node = '26.1-26.1.2-nil';        mc = '26.1'; loader = $null; lv = $null; nil = $true },
    @{ node = '26.2-nil';               mc = '26.2'; loader = $null; lv = $null; nil = $true },
    @{ node = '26.3-nil';               mc = '26.3'; loader = $null; lv = $null; nil = $true },

    # Quilt check: Fabric jar on Quilt.
    @{ node = '26.3-fabric';            mc = '26.3'; loader = 'org.quiltmc.quilt-loader'; lv = '0.30.2'; label = 'Quilt 26.3' }
)

function New-Instance($name, $group, $mc, $loader, $loaderVersion, $jar, $isNil) {
    $dir = Join-Path $instancesDir $name
    if (Test-Path $dir) { Remove-Item $dir -Recurse -Force }
    $mcDir = Join-Path $dir '.minecraft'
    New-Item -ItemType Directory -Force -Path (Join-Path $mcDir 'mods') | Out-Null

    $javaPath = if ($mc -like '26.*') { $Java25 } else { $Java21 }
    $javaVersion = if ($mc -like '26.*') { '25.0.1' } else { '21.0.9' }

    $components = @(@{ uid = 'net.minecraft'; version = $mc; important = $true })
    if ($loader) { $components += @{ uid = $loader; version = $loaderVersion } }
    $pack = [ordered]@{ formatVersion = 1; components = $components }
    Set-Content -Path (Join-Path $dir 'mmc-pack.json') -Value ($pack | ConvertTo-Json -Depth 5)

    $cfg = @(
        'InstanceType=OneSix'
        "name=$name"
        "group=Bundle Inventory"
        'OverrideMemory=true'
        'MinMemAlloc=1024'
        'MaxMemAlloc=4096'
        'OverrideJavaVersion=true'
        "JavaVersion=$javaVersion"
        'JavaArchitecture=64'
        "JavaPath=$javaPath"
    )
    if ($isNil) {
        $cfg += 'OverrideJavaArgs=true'
        $cfg += "JvmArgs=-javaagent:$jar"
    }
    Set-Content -Path (Join-Path $dir 'instance.cfg') -Value $cfg

    $source = Join-Path $Repo "versions\$group\build\libs\$jar"
    if (-not (Test-Path $source)) { throw "missing jar: $source" }
    if ($isNil) {
        Copy-Item $source (Join-Path $mcDir $jar)
    } else {
        Copy-Item $source (Join-Path $mcDir "mods\$jar")
    }
    Write-Output "created: $name  ($mc / $(if ($loader) { $loader + ' ' + $loaderVersion } else { 'NilLoader agent' }))"
}

foreach ($g in $groups) {
    $libs = Join-Path $Repo "versions\$($g.node)\build\libs"
    $jar = (Get-ChildItem $libs -Filter '*.jar' | Where-Object { $_.Name -notmatch 'sources' } | Select-Object -First 1)
    if (-not $jar) { throw "no jar for $($g.node)" }

    if ($g.ContainsKey('label')) {
        $name = "BI $($g.label)"
    } elseif ($g.loader -eq 'net.fabricmc.fabric-loader') {
        $name = "BI Fabric $($g.node -replace '-fabric$', '')"
    } elseif ($g.loader -eq 'net.neoforged') {
        $name = "BI NeoForge $($g.node -replace '-neoforge$', '')"
    } elseif ($g.loader -eq 'net.minecraftforge') {
        $name = "BI Forge $($g.node -replace '-forge$', '')"
    } else {
        $name = "BI NilLoader $($g.node -replace '-nil$', '')"
    }
    New-Instance -name $name -group $g.node -mc $g.mc -loader $g.loader -loaderVersion $g.lv -jar $jar.Name -isNil ([bool]$g.nil)
}

Write-Output ("total instances: " + (Get-ChildItem $instancesDir -Directory | Measure-Object).Count)

# Prism's group assignments are authoritative in instgroups.json (the
# `group=` key in instance.cfg alone does not populate the Groups view), so
# merge the created instances into the "Bundle Inventory" group.
$groupFile = Join-Path $instancesDir 'instgroups.json'
$groupName = 'Bundle Inventory'
$created = Get-ChildItem $instancesDir -Directory | Where-Object { $_.Name -like 'BI *' } | Select-Object -ExpandProperty Name | Sort-Object
$groups = [ordered]@{}
if (Test-Path $groupFile) {
    $existing = Get-Content $groupFile -Raw | ConvertFrom-Json
    foreach ($p in $existing.groups.PSObject.Properties) { $groups[$p.Name] = $p.Value }
}
$groups[$groupName] = [pscustomobject]@{ hidden = $false; instances = @($created) }
[ordered]@{ formatVersion = '1'; groups = $groups } | ConvertTo-Json -Depth 6 | Set-Content $groupFile
Write-Output ("grouped $($created.Count) instances under '$groupName' in instgroups.json")

