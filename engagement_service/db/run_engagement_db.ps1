param(
    [string]$MysqlExe = $(if ($env:MYSQL_EXE) { $env:MYSQL_EXE } else { "mysql" }),
    [string]$DbHost = $(if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { "localhost" }),
    [int]$DbPort = $(if ($env:MYSQL_PORT) { [int]$env:MYSQL_PORT } else { 3306 }),
    [string]$DbUser = $(if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "root" }),
    [string]$DbPassword = $(if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { "1234" }),
    [string]$ScriptPath = $(Join-Path $PSScriptRoot "mysql_sprint2_engagement_schema.sql")
)

$ErrorActionPreference = "Stop"

function Resolve-MysqlExe([string]$Candidate) {
    if (Test-Path $Candidate) {
        return $Candidate
    }

    $command = Get-Command $Candidate -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $commonPaths = @(
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
        "C:\xampp\mysql\bin\mysql.exe"
    )

    foreach ($path in $commonPaths) {
        if (Test-Path $path) {
            return $path
        }
    }

    throw "Khong tim thay mysql.exe. Ban co the truyen -MysqlExe hoac set env MYSQL_EXE."
}

if (-not (Test-Path $ScriptPath)) {
    throw "Khong tim thay file SQL: $ScriptPath"
}

$mysql = Resolve-MysqlExe $MysqlExe

Write-Host "Importing engagement_db from $ScriptPath" -ForegroundColor Cyan
Write-Host "Using MySQL: $mysql" -ForegroundColor DarkGray

Get-Content -Path $ScriptPath -Raw | & $mysql "--default-character-set=utf8mb4" "-h$DbHost" "-P$DbPort" "-u$DbUser" "-p$DbPassword"

if ($LASTEXITCODE -ne 0) {
    throw "Import that bai voi exit code $LASTEXITCODE"
}

Write-Host "engagement_db da duoc import thanh cong." -ForegroundColor Green
