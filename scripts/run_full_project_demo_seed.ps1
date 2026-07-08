param(
    [string]$MysqlExe = $(if ($env:MYSQL_EXE) { $env:MYSQL_EXE } else { "mysql" }),
    [string]$DbHost = $(if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { "localhost" }),
    [int]$DbPort = $(if ($env:MYSQL_PORT) { [int]$env:MYSQL_PORT } else { 3306 }),
    [string]$DbUser = $(if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "root" }),
    [string]$DbPassword = $(if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { "123456" }),
    [string]$ScriptPath = $(Join-Path $PSScriptRoot "full_project_demo_seed.sql")
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

Write-Host "Importing full project demo seed from $ScriptPath" -ForegroundColor Cyan
Write-Host "Using MySQL: $mysql" -ForegroundColor DarkGray
Write-Host "Target: ${DbUser}@${DbHost}:$DbPort" -ForegroundColor DarkGray

Get-Content -Path $ScriptPath -Raw | & $mysql "--default-character-set=utf8mb4" "-h$DbHost" "-P$DbPort" "-u$DbUser" "-p$DbPassword"

if ($LASTEXITCODE -ne 0) {
    throw "Import that bai voi exit code $LASTEXITCODE"
}

Write-Host ""
Write-Host "Full demo seed imported successfully." -ForegroundColor Green
Write-Host "Databases: auth_db, customer_db, engagement_db, production_db" -ForegroundColor Green
Write-Host ""
Write-Host "Demo accounts (password: 123456):" -ForegroundColor Yellow
Write-Host "  admin@gmail.com"
Write-Host "  manager@gmail.com"
Write-Host "  product.manager@example.com"
Write-Host "  marketing.manager@example.com"
Write-Host "  support.agent@example.com"
Write-Host "  customer.demo@example.com"
Write-Host "  customer.demo2@example.com"
Write-Host "  customer.vip@example.com"
Write-Host "  customer.gold@example.com"
Write-Host "  customer.silver@example.com"
Write-Host "  customer.bronze@example.com"
Write-Host "  customer.new@example.com"
Write-Host "  customer.atrisk@example.com"
Write-Host ""
Write-Host "Note: production-service/application.properties currently uses MySQL password 'root' by default." -ForegroundColor Yellow
Write-Host "If your local MySQL password is different, create application-local.properties for production-service." -ForegroundColor Yellow
