param(
    [string]$BaseUrl = $(if ($env:AUTH_BASE_URL) { $env:AUTH_BASE_URL } else { "http://localhost:8081" }),
    [string]$LogPath = $(if ($env:AUTH_LOG_PATH) { $env:AUTH_LOG_PATH } else { (Join-Path $PSScriptRoot "run8081.out") }),
    [string]$MysqlExe = $(if ($env:MYSQL_EXE) { $env:MYSQL_EXE } else { "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" }),
    [string]$DbName = $(if ($env:AUTH_DB_NAME) { $env:AUTH_DB_NAME } else { "auth_db" }),
    [string]$DbUser = $(if ($env:AUTH_DB_USER) { $env:AUTH_DB_USER } else { "root" }),
    [string]$DbPassword = $(if ($env:AUTH_DB_PASSWORD) { $env:AUTH_DB_PASSWORD } else { "root" })
)

$baseUrl = $BaseUrl
$logPath = $LogPath
$mysql = $MysqlExe
$baselineHash = '$2a$10$aoRVRO8iq8gGhZHon9fRU.puI009NRO1ux3oHOynoNEkkUA0x0zIe'
$results = @()

function Invoke-Db([string]$sql) {
    & $mysql "-u$DbUser" "-p$DbPassword" $DbName -e $sql | Out-Null
}

function Invoke-Api($method, $path, $body, $token) {
    $tmp = Join-Path $env:TEMP ([guid]::NewGuid().ToString() + ".txt")
    $jsonFile = $null
    $args = @("-s", "-o", $tmp, "-w", "%{http_code}", "-X", $method, "$baseUrl$path")

    if ($token) {
        $args += @("-H", "Authorization: Bearer $token")
    }
    if ($null -ne $body) {
        $jsonFile = Join-Path $env:TEMP ([guid]::NewGuid().ToString() + ".json")
        ($body | ConvertTo-Json -Compress) | Set-Content -Path $jsonFile -NoNewline
        $args += @("-H", "Content-Type: application/json", "--data-binary", "@$jsonFile")
    }

    $status = & curl.exe @args
    $content = Get-Content $tmp -Raw
    Remove-Item $tmp -Force
    if ($jsonFile -and (Test-Path $jsonFile)) {
        Remove-Item $jsonFile -Force
    }

    [pscustomobject]@{
        Status = [int]$status
        Content = $content
    }
}

function Add-Result($id, $ok, $status, $note) {
    $script:results += [pscustomobject]@{
        ID = $id
        Pass = $ok
        Status = $status
        Note = $note
    }
}

function Get-LatestResetToken() {
    if (-not (Test-Path $logPath)) {
        return $null
    }
    Start-Sleep -Milliseconds 1500
    $matches = Select-String -Path $logPath -Pattern 'Token for testing:\s*([A-Za-z0-9_-]+)' -AllMatches
    if ($matches) {
        return ($matches.Matches | ForEach-Object { $_.Groups[1].Value } | Select-Object -Last 1)
    }
    return $null
}

Invoke-Db @"
DELETE FROM password_reset_tokens WHERE user_id = (SELECT id FROM users WHERE username = 'apitest_story');
DELETE FROM password_reset_requests WHERE email IN ('apitest.story@example.com', 'rate.limit@example.com');
DELETE FROM revoked_tokens;
UPDATE users
SET password = '$baselineHash',
    full_name = 'API Test User',
    phone = '+84900000001',
    avatar_url = 'https://example.com/seed.jpg',
    password_changed_at = DATE_SUB(NOW(), INTERVAL 1 DAY),
    updated_at = NOW()
WHERE username = 'apitest_story';
"@

$login = Invoke-Api "POST" "/api/auth/login" @{ username = "apitest_story"; password = "123456" } $null
$loginObj = $login.Content | ConvertFrom-Json
$token = $loginObj.token
Add-Result "LOGIN" ($login.Status -eq 200 -and $null -ne $token) $login.Status "login baseline"

$r = Invoke-Api "PUT" "/api/accounts/me" @{ fullName = "Story 9 User"; phone = "+84901234567"; avatarUrl = "https://example.com/a.jpg" } $token
$obj = $r.Content | ConvertFrom-Json
Add-Result "UA01" ($r.Status -eq 200 -and $obj.data.fullName -eq "Story 9 User") $r.Status "valid update"

$r = Invoke-Api "PUT" "/api/accounts/me" @{ fullName = "Story 9 User"; phone = "abc"; avatarUrl = "https://example.com/a.jpg" } $token
Add-Result "UA05" ($r.Status -eq 400 -and $r.Content -match "Invalid phone format") $r.Status "invalid phone"

$r = Invoke-Api "PUT" "/api/accounts/me" @{ fullName = ("X" * 101); phone = "+84901234567"; avatarUrl = "https://example.com/a.jpg" } $token
Add-Result "UA06" ($r.Status -eq 400 -and $r.Content -match "Full name too long") $r.Status "full name too long"

$r = Invoke-Api "PUT" "/api/accounts/me" @{ fullName = "Story 9 User"; phone = "+84901234567"; avatarUrl = "abc" } $token
Add-Result "UA07" ($r.Status -eq 400 -and $r.Content -match "Invalid URL format") $r.Status "invalid url"

$r = Invoke-Api "PUT" "/api/accounts/me" @{ phone = "+84901234567"; avatarUrl = "https://example.com/a.jpg" } $token
Add-Result "UA08" ($r.Status -eq 400 -and $r.Content -match "Full name is required") $r.Status "missing fullName"

$r = Invoke-Api "PUT" "/api/accounts/me" @{ fullName = "Story 9 User"; avatarUrl = "https://example.com/a.jpg" } $token
Add-Result "UA09" ($r.Status -eq 400 -and $r.Content -match "Phone is required") $r.Status "missing phone"

$r = Invoke-Api "PUT" "/api/accounts/me" @{ fullName = "Story 9 User"; phone = "+84901234567" } $token
Add-Result "UA10" ($r.Status -eq 400 -and $r.Content -match "Avatar URL is required") $r.Status "missing avatar"

$r = Invoke-Api "PUT" "/api/accounts/me" @{ fullName = "Story 9 User"; phone = "+84901234567"; avatarUrl = "https://example.com/a.jpg" } $null
Add-Result "UA03" (($r.Status -eq 401) -or ($r.Status -eq 403)) $r.Status "no token"

$r = Invoke-Api "PUT" "/api/accounts/me" @{
    fullName = "Story 9 User"
    phone = "+84901234567"
    avatarUrl = "https://example.com/a.jpg"
    email = "hack@gmail.com"
    username = "hack"
    status = "LOCKED"
} $token
$obj = $r.Content | ConvertFrom-Json
Add-Result "UA11" ($r.Status -eq 200 -and $obj.data.email -eq "apitest.story@example.com" -and $obj.data.username -eq "apitest_story") $r.Status "forbidden fields ignored"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/request" @{ email = "apitest.story@example.com" } $null
$tokenA = Get-LatestResetToken
Add-Result "FP01" ($r.Status -eq 200 -and $r.Content -match "Reset link sent to email" -and $null -ne $tokenA) $r.Status "existing email"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/request" @{ email = "notfound@example.com" } $null
Add-Result "FP02" ($r.Status -eq 200 -and $r.Content -match "Reset link sent to email") $r.Status "non-existing email"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/request" @{ email = "abc" } $null
Add-Result "FP03" ($r.Status -eq 400 -and $r.Content -match "Invalid email format") $r.Status "invalid email"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/request" @{ email = "" } $null
Add-Result "FP04" ($r.Status -eq 400 -and $r.Content -match "Email is required") $r.Status "empty email"

Invoke-Db "DELETE FROM password_reset_requests WHERE email = 'rate.limit@example.com';"
1..3 | ForEach-Object { [void](Invoke-Api "POST" "/api/accounts/forgot-password/request" @{ email = "rate.limit@example.com" } $null) }
$r = Invoke-Api "POST" "/api/accounts/forgot-password/request" @{ email = "rate.limit@example.com" } $null
Add-Result "FP05" ($r.Status -eq 429 -and $r.Content -match "Too many reset requests") $r.Status "rate limit"

Invoke-Db @"
DELETE FROM password_reset_tokens WHERE user_id = (SELECT id FROM users WHERE username = 'apitest_story');
DELETE FROM password_reset_requests WHERE email = 'apitest.story@example.com';
"@
$r = Invoke-Api "POST" "/api/accounts/forgot-password/request" @{ email = "apitest.story@example.com" } $null
$expiredToken = Get-LatestResetToken
Invoke-Db "UPDATE password_reset_tokens SET expires_at = DATE_SUB(NOW(), INTERVAL 1 MINUTE) WHERE user_id = (SELECT id FROM users WHERE username = 'apitest_story') ORDER BY id DESC LIMIT 1;"
$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $expiredToken; newPassword = "Abc@1234"; confirmPassword = "Abc@1234" } $null
Add-Result "FP09" ($r.Status -eq 400 -and $r.Content -match "Reset link expired") $r.Status "expired token"

Invoke-Db @"
DELETE FROM password_reset_tokens WHERE user_id = (SELECT id FROM users WHERE username = 'apitest_story');
DELETE FROM password_reset_requests WHERE email = 'apitest.story@example.com';
"@
$r = Invoke-Api "POST" "/api/accounts/forgot-password/request" @{ email = "apitest.story@example.com" } $null
$validToken = Get-LatestResetToken
$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $validToken; newPassword = "Abc@1234"; confirmPassword = "Abc@1234" } $null
Add-Result "FP07" ($r.Status -eq 200 -and $r.Content -match "Password reset successfully") $r.Status "valid reset"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $validToken; newPassword = "Def@1234"; confirmPassword = "Def@1234" } $null
Add-Result "FP10" ($r.Status -eq 400 -and $r.Content -match "Reset link already used") $r.Status "used token"

$r = Invoke-Api "POST" "/api/auth/login" @{ username = "apitest_story"; password = "123456" } $null
Add-Result "FP22" ($r.Status -ne 200) $r.Status "old password fails"

$r = Invoke-Api "POST" "/api/auth/login" @{ username = "apitest_story"; password = "Abc@1234" } $null
$newToken = ($r.Content | ConvertFrom-Json).token
Add-Result "FP23" ($r.Status -eq 200 -and $null -ne $newToken) $r.Status "new password works"

$r = Invoke-Api "PUT" "/api/accounts/me" @{ fullName = "After Reset"; phone = "+84901234568"; avatarUrl = "https://example.com/b.jpg" } $token
Add-Result "FP24" ($r.Status -eq 401 -and $r.Content -match "Token expired") $r.Status "old access token invalid"

Invoke-Db @"
DELETE FROM password_reset_tokens WHERE user_id = (SELECT id FROM users WHERE username = 'apitest_story');
DELETE FROM password_reset_requests WHERE email = 'apitest.story@example.com';
"@
$r = Invoke-Api "POST" "/api/accounts/forgot-password/request" @{ email = "apitest.story@example.com" } $null
$tokenB = Get-LatestResetToken

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = "badtoken"; newPassword = "Def@1234"; confirmPassword = "Def@1234" } $null
Add-Result "FP08" ($r.Status -eq 400 -and $r.Content -match "Invalid reset link") $r.Status "invalid token"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $tokenB; newPassword = "Def@1234"; confirmPassword = "xyz" } $null
Add-Result "FP11" ($r.Status -eq 400 -and $r.Content -match "Passwords do not match") $r.Status "confirm mismatch"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $tokenB; newPassword = "Ab@12"; confirmPassword = "Ab@12" } $null
Add-Result "FP12" ($r.Status -eq 400 -and $r.Content -match "Password must be at least 8 characters") $r.Status "short password"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $tokenB; newPassword = "abc@1234"; confirmPassword = "abc@1234" } $null
Add-Result "FP13" ($r.Status -eq 400 -and $r.Content -match "Password must be at least 8 characters") $r.Status "missing uppercase"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $tokenB; newPassword = "ABC@1234"; confirmPassword = "ABC@1234" } $null
Add-Result "FP14" ($r.Status -eq 400 -and $r.Content -match "Password must be at least 8 characters") $r.Status "missing lowercase"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $tokenB; newPassword = "Abc@defg"; confirmPassword = "Abc@defg" } $null
Add-Result "FP15" ($r.Status -eq 400 -and $r.Content -match "Password must be at least 8 characters") $r.Status "missing number"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $tokenB; newPassword = "Abc12345"; confirmPassword = "Abc12345" } $null
Add-Result "FP16" ($r.Status -eq 400 -and $r.Content -match "Password must be at least 8 characters") $r.Status "missing special"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $tokenB; newPassword = "Abc@1234"; confirmPassword = "Abc@1234" } $null
Add-Result "FP17" ($r.Status -eq 400 -and $r.Content -match "New password must be different") $r.Status "same as current"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = ""; token = $tokenB; newPassword = "Def@1234"; confirmPassword = "Def@1234" } $null
Add-Result "FP18" ($r.Status -eq 400 -and $r.Content -match "Email is required") $r.Status "missing email"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; newPassword = "Def@1234"; confirmPassword = "Def@1234" } $null
Add-Result "FP19" ($r.Status -eq 400 -and $r.Content -match "Token is required") $r.Status "missing token"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $tokenB; confirmPassword = "Def@1234" } $null
Add-Result "FP20" ($r.Status -eq 400 -and $r.Content -match "New password is required") $r.Status "missing newPassword"

$r = Invoke-Api "POST" "/api/accounts/forgot-password/reset" @{ email = "apitest.story@example.com"; token = $tokenB; newPassword = "Def@1234" } $null
Add-Result "FP21" ($r.Status -eq 400 -and $r.Content -match "Confirm password is required") $r.Status "missing confirmPassword"

Invoke-Db @"
DELETE FROM password_reset_tokens WHERE user_id = (SELECT id FROM users WHERE username = 'apitest_story');
DELETE FROM password_reset_requests WHERE email IN ('apitest.story@example.com', 'rate.limit@example.com');
UPDATE users
SET password = '$baselineHash',
    full_name = 'API Test User',
    phone = '+84900000001',
    avatar_url = 'https://example.com/seed.jpg',
    password_changed_at = DATE_SUB(NOW(), INTERVAL 1 DAY),
    updated_at = NOW()
WHERE username = 'apitest_story';
"@

$results | Format-Table -AutoSize
