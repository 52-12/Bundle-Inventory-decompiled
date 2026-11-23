# Installs the optional-integration mods into the Fabric playtest instances so
# checklist step 9 (Mod Menu -> Bundle Inventory -> config screen) can run:
#   - Fabric API (Mod Menu needs it; the full bundle, not just the modules our
#     own jar nests)
#   - Mod Menu               (provides the mods screen + config button)
#   - Cloth Config (Fabric)  (the config screen implementation the mod uses)
#
# Versions come from stonecutter.properties.toml, so they always match the node
# they belong to. The Quilt check instance is left alone on purpose: it verifies
# the mod jar runs with no Fabric API present.
#
#   powershell -File ..\tools\install-playtest-mods.ps1
param(
    [string]$Repo = '',
    [string]$PrismData = "$env:APPDATA\PrismLauncher"
)
$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
if (-not $Repo) {
    if (Test-Path (Join-Path (Get-Location) 'gradlew.bat')) { $Repo = (Get-Location).Path }
    else { $Repo = Split-Path -Parent $PSScriptRoot }
}
$instanceRoot = Join-Path $PrismData 'instances'
$cfgPath = Join-Path $PrismData 'prismlauncher.cfg'
if (Test-Path $cfgPath) {
    $line = Get-Content $cfgPath | Where-Object { $_ -match '^InstanceDir=' } | Select-Object -First 1
    if ($line) { $instanceRoot = ($line -replace '^InstanceDir=', '') }
}

# --------------------------------------------------------------- versions ---
$toml = Get-Content (Join-Path $Repo 'stonecutter.properties.toml') -Raw
$groups = @()
foreach ($m in [regex]::Matches($toml, '(?ms)^\["(?<node>[^"]+)"\]\s*(?<body>.*?)(?=^\["|\z)')) {
    $node = $m.Groups['node'].Value
    if ($node -notmatch '-fabric$') { continue }
    $body = $m.Groups['body'].Value
    $cloth = [regex]::Match($body, 'deps\.cloth_config = "([^"]+)"').Groups[1].Value
    $menu = [regex]::Match($body, 'deps\.modmenu = "([^"]+)"').Groups[1].Value
    $api = [regex]::Match($body, 'deps\.fabric_api = "([^"]+)"').Groups[1].Value
    $label = $node -replace '-fabric$', ''
    $groups += [pscustomobject]@{ Label = $label; Instance = "BI Fabric $label"; Api = $api; ModMenu = $menu; Cloth = $cloth }
}

# ---------------------------------------------------------------- sources ---
$cache = 'C:\Users\BL\.gradle\caches\modules-2\files-2.1'
function Find-InCache([string]$relative, [string]$jarName) {
    $root = Join-Path $cache $relative
    if (-not (Test-Path $root)) { return $null }
    $jar = Get-ChildItem $root -Recurse -Filter $jarName | Select-Object -First 1
    if ($jar) { return $jar.FullName }
    return $null
}

function Get-FabricApiJar([string]$version) {
    $dir = Join-Path $env:TEMP 'bundleinventory-fabric-api'
    New-Item -ItemType Directory -Force $dir | Out-Null
    $dest = Join-Path $dir "fabric-api-$version.jar"
    if (-not (Test-Path $dest)) {
        $encoded = $version -replace '\+', '%2B'
        $url = "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$encoded/fabric-api-$encoded.jar"
        Invoke-WebRequest $url -OutFile $dest -UseBasicParsing
    }
    return $dest
}

function Get-ModMenuJar([string]$version) {
    $hit = Find-InCache "com.terraformersmc\modmenu\$version" "modmenu-$version.jar"
    if ($hit) { return $hit }
    $dir = Join-Path $env:TEMP 'bundleinventory-modmenu'
    New-Item -ItemType Directory -Force $dir | Out-Null
    $dest = Join-Path $dir "modmenu-$version.jar"
    if (-not (Test-Path $dest)) {
        Invoke-WebRequest "https://maven.terraformersmc.com/releases/com/terraformersmc/modmenu/$version/modmenu-$version.jar" -OutFile $dest -UseBasicParsing
    }
    return $dest
}

function Get-ClothJar([string]$version) {
    $root = Join-Path $cache "me.shedaniel.cloth\cloth-config-fabric\$version"
    if (Test-Path $root) {
        $hit = Get-ChildItem $root -Recurse -Filter '*.jar' | Where-Object { $_.Name -notlike '*sources*' } | Select-Object -First 1
        if ($hit) { return $hit.FullName }
    }
    if ($hit) { return $hit }
    $dir = Join-Path $env:TEMP 'bundleinventory-cloth'
    New-Item -ItemType Directory -Force $dir | Out-Null
    foreach ($name in @("cloth-config-fabric-$version-fabric.jar", "cloth-config-fabric-$version.jar")) {
        $dest = Join-Path $dir $name
        if (-not (Test-Path $dest)) {
            try {
                Invoke-WebRequest "https://maven.shedaniel.me/me/shedaniel/cloth/cloth-config-fabric/$version/$name" -OutFile $dest -UseBasicParsing -ErrorAction Stop
            } catch { continue }
        }
        return $dest
    }
    throw "cloth-config-fabric $version not found"
}

# ---------------------------------------------------------------- install ---
foreach ($group in $groups) {
    $mods = Join-Path $instanceRoot "$($group.Instance)\.minecraft\mods"
    if (-not (Test-Path (Split-Path -Parent $mods))) {
        Write-Warning "$($group.Instance) does not exist, skipped"
        continue
    }
    New-Item -ItemType Directory -Force $mods | Out-Null
    Get-ChildItem $mods -File | Where-Object {
        $_.Name -like 'fabric-api-*.jar' -or $_.Name -like 'modmenu-*.jar' -or $_.Name -like 'cloth-config-*.jar'
    } | Remove-Item -Force

    $jars = @(
        Get-FabricApiJar $group.Api
        Get-ModMenuJar $group.ModMenu
        Get-ClothJar $group.Cloth
    )
    foreach ($jar in $jars) { Copy-Item $jar (Join-Path $mods (Split-Path $jar -Leaf)) -Force }
    Write-Output ("{0,-24} api {1,-18} modmenu {2,-10} cloth {3}" -f $group.Instance, $group.Api, $group.ModMenu, $group.Cloth)
}
Write-Output "`nFabric playtest instances now include Fabric API, Mod Menu and Cloth Config."
Write-Output "The Quilt check instance intentionally has only the mod jar."
