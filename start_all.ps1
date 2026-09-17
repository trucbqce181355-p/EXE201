$services = @(
    @{ Name = "auth-service"; Command = ".\mvnw.cmd spring-boot:run" },
    @{ Name = "customer-service"; Command = ".\mvnw.cmd spring-boot:run" },
    @{ Name = "engagement_service"; Command = ".\mvnw.cmd spring-boot:run" },
    @{ Name = "production-service"; Command = ".\mvnw.cmd spring-boot:run" },
    @{ Name = "frontend"; Command = "npm.cmd install; npm.cmd run dev" }
)

Write-Host "Starting all services..." -ForegroundColor Green

foreach ($service in $services) {
    $dir = Join-Path $PSScriptRoot $service.Name
    $cmd = $service.Command
    $title = $service.Name
    
    Write-Host "Starting $title in a new window..." -ForegroundColor Cyan
    Start-Process powershell -WorkingDirectory $dir -ArgumentList "-ExecutionPolicy Bypass -NoExit -Command `"title $title; $cmd`""
}

Write-Host "All services started!" -ForegroundColor Green
