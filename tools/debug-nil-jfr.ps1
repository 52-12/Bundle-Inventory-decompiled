# Temporary debugging helper: record JFR (packets + CPU samples) from a running
# NilLoader playtest client, then summarise it.
#
#   powershell -File tools\debug-nil-jfr.ps1 start     # then reproduce the lag
#   powershell -File tools\debug-nil-jfr.ps1 stop      # stop + report
#   powershell -File tools\debug-nil-jfr.ps1 status
#
# JFR is built into the JDK Minecraft already runs on; nothing is added to the
# game or the mod. Packet events show whether a click is answered with a full
# container resync, and ExecutionSample shows what the client thread is doing.
param(
    [ValidateSet('start', 'stop', 'status')]
    [string]$Action = 'status',
    [string]$Instance = 'BI NilLoader 26.3'
)
$ErrorActionPreference = 'Stop'
$stateFile = Join-Path $PSScriptRoot '.jfr-current.json'

function Get-GameProcess {
    Get-CimInstance Win32_Process -Filter "Name='javaw.exe'" |
        Where-Object { $_.CommandLine -like "*$Instance*" } |
        Select-Object -First 1
}

function Get-Tools([string]$exePath) {
    $bin = Split-Path $exePath -Parent
    [pscustomobject]@{
        Jcmd = Join-Path $bin 'jcmd.exe'
        Jfr  = Join-Path $bin 'jfr.exe'
    }
}

switch ($Action) {
    'start' {
        $game = Get-GameProcess
        if (-not $game) { throw "Instance '$Instance' is not running" }
        $tools = Get-Tools $game.ExecutablePath
        $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
        $dir = Join-Path $env:TEMP "bundleinventory-jfr-$stamp"
        New-Item -ItemType Directory -Force $dir | Out-Null
        $recording = Join-Path $dir 'recording.jfr'
        & $tools.Jcmd $game.ProcessId JFR.start "name=bundleinventory" "settings=profile" "disk=true" "filename=$recording"
        [pscustomobject]@{
            Pid       = $game.ProcessId
            Directory = $dir
            Recording = $recording
            Jcmd      = $tools.Jcmd
            Jfr       = $tools.Jfr
        } | ConvertTo-Json | Set-Content $stateFile
        Write-Output "JFR recording started (pid $($game.ProcessId))."
        Write-Output "Reproduce the insert/remove lag now."
        Write-Output "Then run:  powershell -File tools\debug-nil-jfr.ps1 stop"
    }
    'stop' {
        if (-not (Test-Path $stateFile)) { throw 'No active recording (run start first)' }
        $state = Get-Content $stateFile -Raw | ConvertFrom-Json
        try {
            & $state.Jcmd $state.Pid JFR.stop "name=bundleinventory"
        } catch {
            & $state.Jcmd $state.Pid JFR.dump "name=bundleinventory" "filename=$($state.Recording)"
        }
        Start-Sleep -Seconds 2
        if (-not (Test-Path $state.Recording)) { throw "Recording not written: $($state.Recording)" }
        $summary = Join-Path $state.Directory 'summary.txt'
        & $state.Jfr summary $state.Recording *> $summary
        $packets = Join-Path $state.Directory 'packets.json'
        & $state.Jfr print --events 'minecraft.PacketSent,minecraft.PacketReceived' --json $state.Recording *> $packets
        $cpu = Join-Path $state.Directory 'cpu.json'
        & $state.Jfr print --events 'jdk.ExecutionSample' --json $state.Recording *> $cpu
        Write-Output ''
        node (Join-Path $PSScriptRoot 'jfr-report.mjs') $packets $cpu
        Write-Output ''
        Write-Output "Files: $($state.Directory)"
        Remove-Item $stateFile -Force
    }
    'status' {
        $game = Get-GameProcess
        if ($game) {
            Write-Output "instance running: pid $($game.ProcessId)  $($game.ExecutablePath)"
        } else {
            Write-Output "instance '$Instance' is not running"
        }
        if (Test-Path $stateFile) {
            $state = Get-Content $stateFile -Raw | ConvertFrom-Json
            Write-Output "active recording: $($state.Recording)"
        }
    }
}
