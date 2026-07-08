param(
    [ValidateSet("quick", "full")]
    [string]$Mode = "quick",
    [string]$AuthBaseUrl = $(if ($env:AUTH_BASE_URL) { $env:AUTH_BASE_URL } else { "http://localhost:8081" }),
    [string]$EngagementBaseUrl = $(if ($env:ENGAGEMENT_BASE_URL) { $env:ENGAGEMENT_BASE_URL } else { "http://localhost:8083" }),
    [string]$MysqlExe = $(if ($env:MYSQL_EXE) { $env:MYSQL_EXE } else { "mysql" }),
    [string]$DbHost = $(if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { "localhost" }),
    [int]$DbPort = $(if ($env:MYSQL_PORT) { [int]$env:MYSQL_PORT } else { 3306 }),
    [string]$DbUser = $(if ($env:AUTH_DB_USER) { $env:AUTH_DB_USER } else { "root" }),
    [string]$DbPassword = $(if ($env:AUTH_DB_PASSWORD) { $env:AUTH_DB_PASSWORD } else { "1234" }),
    [string]$AdminEmail = $(if ($env:LOYALTY_ADMIN_EMAIL) { $env:LOYALTY_ADMIN_EMAIL } else { "admin@gmail.com" }),
    [string]$AdminPassword = $(if ($env:LOYALTY_ADMIN_PASSWORD) { $env:LOYALTY_ADMIN_PASSWORD } else { "123456" }),
    [string]$UserEmail = $(if ($env:LOYALTY_USER_EMAIL) { $env:LOYALTY_USER_EMAIL } else { "loyalty.user@example.com" }),
    [string]$UserPassword = $(if ($env:LOYALTY_USER_PASSWORD) { $env:LOYALTY_USER_PASSWORD } else { "123456" }),
    [switch]$SkipPermissionSeed,
    [switch]$SkipRestore
)

$ErrorActionPreference = "Stop"
$results = @()
$adminToken = $null
$originalConfig = $null
$originalConfigCaptured = $false
$fatalError = $null
$runFull = $Mode -eq "full"

function Add-Result($id, $ok, $status, $note) {
    $script:results += [pscustomobject]@{
        ID = $id
        Pass = $ok
        Status = $status
        Note = $note
    }
}

function Try-ParseJson([string]$content) {
    if ([string]::IsNullOrWhiteSpace($content)) {
        return $null
    }

    try {
        return $content | ConvertFrom-Json
    } catch {
        return $null
    }
}

function Resolve-MysqlExe([string]$candidate) {
    if (Test-Path $candidate) {
        return $candidate
    }

    $command = Get-Command $candidate -ErrorAction SilentlyContinue
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

    throw "Could not find mysql.exe. Pass -MysqlExe or set MYSQL_EXE."
}

function Seed-LoyaltyPermissions {
    if ($SkipPermissionSeed) {
        return
    }

    $seedPath = Join-Path (Split-Path $PSScriptRoot -Parent) "auth-service\db\mysql_seed_loyalty_permissions.sql"
    if (-not (Test-Path $seedPath)) {
        throw "Permission seed file not found: $seedPath"
    }

    $mysql = Resolve-MysqlExe $MysqlExe
    Get-Content -Path $seedPath -Raw | & $mysql "--default-character-set=utf8mb4" "-h$DbHost" "-P$DbPort" "-u$DbUser" "-p$DbPassword"

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to seed loyalty permissions into auth_db"
    }
}

function Invoke-DbScript([string]$sql) {
    $mysql = Resolve-MysqlExe $MysqlExe
    $sql | & $mysql "--default-character-set=utf8mb4" "-h$DbHost" "-P$DbPort" "-u$DbUser" "-p$DbPassword"

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to execute auth_db seed SQL"
    }
}

function Ensure-RestrictedTestUser {
    $username = "loyalty_test_user"
    $passwordHash = '$2a$12$mlUS8rKIPEPPkM8YG.CYMuLj9yPAAFS6BkuGcjqdECFsshFb/t6JS'

    Invoke-DbScript @"
USE auth_db;

INSERT IGNORE INTO roles (name, description)
VALUES ('USER', 'Standard user');

INSERT INTO users (
    email,
    username,
    password,
    full_name,
    phone,
    avatar_url,
    status,
    password_changed_at,
    created_at,
    updated_at
) VALUES (
    '$UserEmail',
    '$username',
    '$passwordHash',
    'Loyalty Restricted User',
    '0900000099',
    NULL,
    'ACTIVE',
    NOW(),
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    full_name = VALUES(full_name),
    phone = VALUES(phone),
    avatar_url = VALUES(avatar_url),
    status = VALUES(status),
    password_changed_at = NOW(),
    updated_at = NOW();

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'USER'
WHERE u.email = '$UserEmail';
"@
}

function Invoke-Api($method, $baseUrl, $path, $body, $token) {
    $tmp = Join-Path $env:TEMP ([guid]::NewGuid().ToString() + ".txt")
    $jsonFile = $null
    $args = @(
        "-s",
        "-o", $tmp,
        "-w", "%{http_code}",
        "--connect-timeout", "5",
        "-X", $method,
        "$baseUrl$path"
    )

    if ($token) {
        $args += @("-H", "Authorization: Bearer $token")
    }

    if ($null -ne $body) {
        $jsonFile = Join-Path $env:TEMP ([guid]::NewGuid().ToString() + ".json")
        if ($body -is [string]) {
            Set-Content -Path $jsonFile -Value $body -NoNewline
        } else {
            ($body | ConvertTo-Json -Depth 20 -Compress) | Set-Content -Path $jsonFile -NoNewline
        }
        $args += @("-H", "Content-Type: application/json", "--data-binary", "@$jsonFile")
    }

    $status = & curl.exe @args
    $exitCode = $LASTEXITCODE
    $content = if (Test-Path $tmp) { Get-Content $tmp -Raw } else { "" }

    if (Test-Path $tmp) {
        Remove-Item $tmp -Force
    }
    if ($jsonFile -and (Test-Path $jsonFile)) {
        Remove-Item $jsonFile -Force
    }

    [pscustomobject]@{
        Status = if ($status) { [int]$status } else { 0 }
        ExitCode = $exitCode
        Content = $content
        Json = Try-ParseJson $content
    }
}

function Assert-MessageContains($response, [string]$expectedText) {
    if ($null -eq $response.Json) {
        return $false
    }

    $message = $response.Json.message
    return $message -and $message -like "*$expectedText*"
}

function Get-TierIds($config) {
    return @($config.tiers | Sort-Object min_points | Select-Object -ExpandProperty id)
}

function Get-TierIdByName($config, [string]$name) {
    return (($config.tiers | Where-Object { $_.name -eq $name }) | Select-Object -First 1).id
}

function Get-BenefitIdByDescription($benefits, [string]$description, [bool]$directOnly = $true) {
    return (($benefits | Where-Object {
                $_.description -eq $description -and (($directOnly -and $_.inherited -eq $false) -or (-not $directOnly))
            }) | Select-Object -First 1).id
}

function Build-RestorePointsBody($config) {
    return @{
        points_per_currency = $config.points_per_currency
        min_order_amount = $config.min_order_amount
        excluded_categories = @($config.excluded_categories)
    }
}

function Build-RestoreExpirationBody($config) {
    return @{
        expiration_months = $config.expiration_months
        evaluation_period_months = $config.evaluation_period_months
    }
}

function Build-RestoreTiersBody($config) {
    $tiers = @()
    foreach ($tier in ($config.tiers | Sort-Object min_points)) {
        $benefits = @()
        foreach ($benefit in @($tier.benefits)) {
            $benefits += @{
                type = $benefit.type
                value = $benefit.value
                description = $benefit.description
            }
        }

        $tiers += @{
            id = $tier.id
            name = $tier.name
            min_points = $tier.min_points
            max_points = $tier.max_points
            benefits = $benefits
        }
    }

    return @{
        inherit_from_lower_tiers = $config.inherit_from_lower_tiers
        tiers = $tiers
    }
}

try {
    Seed-LoyaltyPermissions
    Ensure-RestrictedTestUser

    $unauthorized = Invoke-Api "GET" $EngagementBaseUrl "/loyalty/config" $null $null
    Add-Result "AUTH01" ($unauthorized.Status -eq 401) $unauthorized.Status "GET /loyalty/config without token should return 401"

    $adminLogin = Invoke-Api "POST" $AuthBaseUrl "/api/auth/login" @{
        email = $AdminEmail
        password = $AdminPassword
    } $null
    $adminToken = $adminLogin.Json.accessToken
    Add-Result "AUTH02" ($adminLogin.Status -eq 200 -and $null -ne $adminToken) $adminLogin.Status "Admin login should succeed"

    $userLogin = Invoke-Api "POST" $AuthBaseUrl "/api/auth/login" @{
        email = $UserEmail
        password = $UserPassword
    } $null
    $userToken = $userLogin.Json.accessToken
    Add-Result "AUTH03" ($userLogin.Status -eq 200 -and $null -ne $userToken) $userLogin.Status "Regular user login should succeed"

    $forbidden = Invoke-Api "GET" $EngagementBaseUrl "/loyalty/config" $null $userToken
    Add-Result "AUTH04" ($forbidden.Status -eq 403) $forbidden.Status "User without loyalty authority should get 403"

    $configResponse = Invoke-Api "GET" $EngagementBaseUrl "/loyalty/config" $null $adminToken
    $originalConfig = $configResponse.Json
    $originalConfigCaptured = $configResponse.Status -eq 200 -and $null -ne $originalConfig
    Add-Result "LC01" ($originalConfigCaptured -and @($originalConfig.tiers).Count -ge 4) $configResponse.Status "Initial loyalty config should load with tiers"

    if (-not $originalConfigCaptured) {
        throw "Cannot continue without loading the original loyalty configuration."
    }

    $pointsValid = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/points" @{
        points_per_currency = 1.5
        min_order_amount = 50000
        excluded_categories = @("FLASH_SALE", "GIFT_CARD")
    } $adminToken
    $pointsValidOk = $pointsValid.Status -eq 200 `
        -and [decimal]$pointsValid.Json.points_per_currency -eq [decimal]"1.5" `
        -and [decimal]$pointsValid.Json.min_order_amount -eq [decimal]"50000" `
        -and @($pointsValid.Json.excluded_categories).Count -eq 2
    Add-Result "LC02" $pointsValidOk $pointsValid.Status "Valid point config update should persist values"

    $pointsInvalid = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/points" @{
        points_per_currency = 0
        min_order_amount = 0
        excluded_categories = @()
    } $adminToken
    Add-Result "LC03" ($pointsInvalid.Status -eq 400 -and (Assert-MessageContains $pointsInvalid "greater than 0")) $pointsInvalid.Status "points_per_currency <= 0 should return 400"

    if ($runFull) {
        $pointsNegativeMin = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/points" @{
            points_per_currency = 1
            min_order_amount = -1
            excluded_categories = @()
        } $adminToken
        Add-Result "LC03A" ($pointsNegativeMin.Status -eq 400 -and (Assert-MessageContains $pointsNegativeMin "greater than or equal to 0")) $pointsNegativeMin.Status "min_order_amount < 0 should return 400"

        $pointsMissingRequired = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/points" @{
            min_order_amount = 100
            excluded_categories = @()
        } $adminToken
        Add-Result "LC03B" ($pointsMissingRequired.Status -eq 400 -and (Assert-MessageContains $pointsMissingRequired "Points per currency is required")) $pointsMissingRequired.Status "Missing points_per_currency should return 400"
    }

    $expirationValid = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/expiration" @{
        expiration_months = 12
        evaluation_period_months = 6
    } $adminToken
    $expirationValidOk = $expirationValid.Status -eq 200 `
        -and [int]$expirationValid.Json.expiration_months -eq 12 `
        -and [int]$expirationValid.Json.evaluation_period_months -eq 6
    Add-Result "LC04" $expirationValidOk $expirationValid.Status "Valid expiration config update should persist values"

    $expirationInvalid = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/expiration" @{
        expiration_months = 0
        evaluation_period_months = 6
    } $adminToken
    Add-Result "LC05" ($expirationInvalid.Status -eq 400 -and (Assert-MessageContains $expirationInvalid "greater than or equal to 1")) $expirationInvalid.Status "expiration_months < 1 should return 400"

    if ($runFull) {
        $expirationMissingRequired = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/expiration" @{
            expiration_months = 12
        } $adminToken
        Add-Result "LC05A" ($expirationMissingRequired.Status -eq 400 -and (Assert-MessageContains $expirationMissingRequired "Evaluation period months is required")) $expirationMissingRequired.Status "Missing evaluation_period_months should return 400"
    }

    $tierIds = Get-TierIds $originalConfig
    if ($tierIds.Count -lt 4) {
        throw "Expected at least 4 tiers to run the tier configuration tests."
    }

    $validTierBody = @{
        inherit_from_lower_tiers = $true
        tiers = @(
            @{ id = $tierIds[0]; name = "BRONZE"; min_points = 0; max_points = 999 }
            @{ id = $tierIds[1]; name = "SILVER"; min_points = 1000; max_points = 2999 }
            @{ id = $tierIds[2]; name = "GOLD"; min_points = 3000; max_points = 6999 }
            @{ id = $tierIds[3]; name = "PLATINUM"; min_points = 7000; max_points = $null }
        )
    }
    $tiersValid = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" $validTierBody $adminToken
    $tiersValidOk = $tiersValid.Status -eq 200 `
        -and $tiersValid.Json.inherit_from_lower_tiers `
        -and [int]$tiersValid.Json.tiers[1].max_points -eq 2999 `
        -and [int]$tiersValid.Json.tiers[2].min_points -eq 3000
    Add-Result "LC06" $tiersValidOk $tiersValid.Status "Valid tier ranges should save and enable inheritance"

    $tiersInvalid = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" @{
        inherit_from_lower_tiers = $true
        tiers = @(
            @{ id = $tierIds[0]; name = "BRONZE"; min_points = 0; max_points = 999 }
            @{ id = $tierIds[1]; name = "SILVER"; min_points = 1200; max_points = 2999 }
            @{ id = $tierIds[2]; name = "GOLD"; min_points = 3000; max_points = 6999 }
            @{ id = $tierIds[3]; name = "PLATINUM"; min_points = 7000; max_points = $null }
        )
    } $adminToken
    Add-Result "LC07" ($tiersInvalid.Status -eq 400 -and (Assert-MessageContains $tiersInvalid "continuous and non-overlapping")) $tiersInvalid.Status "Non-continuous tier ranges should return 400"

    if ($runFull) {
        $tiersArrayPayload = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" @(
            @{ id = $tierIds[0]; name = "BRONZE"; min_points = 0; max_points = 999 }
            @{ id = $tierIds[1]; name = "SILVER"; min_points = 1000; max_points = 2999 }
            @{ id = $tierIds[2]; name = "GOLD"; min_points = 3000; max_points = 6999 }
            @{ id = $tierIds[3]; name = "PLATINUM"; min_points = 7000; max_points = $null }
        ) $adminToken
        Add-Result "LC08" ($tiersArrayPayload.Status -eq 200 -and $tiersArrayPayload.Json.inherit_from_lower_tiers) $tiersArrayPayload.Status "Array-only tier payload should be accepted"

        $tiersOverlap = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" @{
            inherit_from_lower_tiers = $true
            tiers = @(
                @{ id = $tierIds[0]; name = "BRONZE"; min_points = 0; max_points = 1500 }
                @{ id = $tierIds[1]; name = "SILVER"; min_points = 1000; max_points = 2999 }
                @{ id = $tierIds[2]; name = "GOLD"; min_points = 3000; max_points = 6999 }
                @{ id = $tierIds[3]; name = "PLATINUM"; min_points = 7000; max_points = $null }
            )
        } $adminToken
        Add-Result "LC09" ($tiersOverlap.Status -eq 400 -and (Assert-MessageContains $tiersOverlap "continuous and non-overlapping")) $tiersOverlap.Status "Overlapping tier ranges should return 400"

        $tiersNotStartAtZero = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" @{
            inherit_from_lower_tiers = $true
            tiers = @(
                @{ id = $tierIds[0]; name = "BRONZE"; min_points = 100; max_points = 999 }
                @{ id = $tierIds[1]; name = "SILVER"; min_points = 1000; max_points = 2999 }
                @{ id = $tierIds[2]; name = "GOLD"; min_points = 3000; max_points = 6999 }
                @{ id = $tierIds[3]; name = "PLATINUM"; min_points = 7000; max_points = $null }
            )
        } $adminToken
        Add-Result "LC10" ($tiersNotStartAtZero.Status -eq 400 -and (Assert-MessageContains $tiersNotStartAtZero "start from 0")) $tiersNotStartAtZero.Status "Tier ranges must start from 0"

        $tiersMiddleNullMax = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" @{
            inherit_from_lower_tiers = $true
            tiers = @(
                @{ id = $tierIds[0]; name = "BRONZE"; min_points = 0; max_points = 999 }
                @{ id = $tierIds[1]; name = "SILVER"; min_points = 1000; max_points = $null }
                @{ id = $tierIds[2]; name = "GOLD"; min_points = 3000; max_points = 6999 }
                @{ id = $tierIds[3]; name = "PLATINUM"; min_points = 7000; max_points = $null }
            )
        } $adminToken
        Add-Result "LC11" ($tiersMiddleNullMax.Status -eq 400 -and (Assert-MessageContains $tiersMiddleNullMax "Only the highest tier can have no max_points")) $tiersMiddleNullMax.Status "Only the highest tier may have null max_points"

        $tiersDuplicateName = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" @{
            inherit_from_lower_tiers = $true
            tiers = @(
                @{ id = $tierIds[0]; name = "BRONZE"; min_points = 0; max_points = 999 }
                @{ id = $tierIds[1]; name = "BRONZE"; min_points = 1000; max_points = 2999 }
                @{ id = $tierIds[2]; name = "GOLD"; min_points = 3000; max_points = 6999 }
                @{ id = $tierIds[3]; name = "PLATINUM"; min_points = 7000; max_points = $null }
            )
        } $adminToken
        Add-Result "LC12" ($tiersDuplicateName.Status -eq 409 -and (Assert-MessageContains $tiersDuplicateName "Tier name already exists")) $tiersDuplicateName.Status "Duplicate tier names should return 409"

        $tiersDuplicateId = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" @{
            inherit_from_lower_tiers = $true
            tiers = @(
                @{ id = $tierIds[0]; name = "BRONZE"; min_points = 0; max_points = 999 }
                @{ id = $tierIds[0]; name = "SILVER"; min_points = 1000; max_points = 2999 }
                @{ id = $tierIds[2]; name = "GOLD"; min_points = 3000; max_points = 6999 }
                @{ id = $tierIds[3]; name = "PLATINUM"; min_points = 7000; max_points = $null }
            )
        } $adminToken
        Add-Result "LC13" ($tiersDuplicateId.Status -eq 400 -and (Assert-MessageContains $tiersDuplicateId "Tier ids must be unique")) $tiersDuplicateId.Status "Duplicate tier ids should return 400"

        $tiersMaxLessThanMin = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" @{
            inherit_from_lower_tiers = $true
            tiers = @(
                @{ id = $tierIds[0]; name = "BRONZE"; min_points = 0; max_points = 999 }
                @{ id = $tierIds[1]; name = "SILVER"; min_points = 1000; max_points = 900 }
                @{ id = $tierIds[2]; name = "GOLD"; min_points = 3000; max_points = 6999 }
                @{ id = $tierIds[3]; name = "PLATINUM"; min_points = 7000; max_points = $null }
            )
        } $adminToken
        Add-Result "LC14" ($tiersMaxLessThanMin.Status -eq 400 -and (Assert-MessageContains $tiersMaxLessThanMin "greater than or equal to min_points")) $tiersMaxLessThanMin.Status "Tier max_points < min_points should return 400"

        $tiersInvalidFormat = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" '"bad"' $adminToken
        Add-Result "LC15" ($tiersInvalidFormat.Status -eq 400 -and (Assert-MessageContains $tiersInvalidFormat "Invalid tier configuration format")) $tiersInvalidFormat.Status "Invalid tier payload format should return 400"
    }

    $configAfterTierUpdate = (Invoke-Api "GET" $EngagementBaseUrl "/loyalty/config" $null $adminToken).Json
    $bronzeId = Get-TierIdByName $configAfterTierUpdate "BRONZE"
    $silverId = Get-TierIdByName $configAfterTierUpdate "SILVER"

    if ($runFull) {
        $forbiddenBenefit = Invoke-Api "POST" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits" @{
            type = "DISCOUNT"
            value = 10
            description = "Forbidden"
        } $userToken
        Add-Result "AUTH05" ($forbiddenBenefit.Status -eq 403) $forbiddenBenefit.Status "User without loyalty benefit authority should get 403 on benefit creation"
    }

    $benefitCreate = Invoke-Api "POST" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits" @{
        type = "DISCOUNT"
        value = 10
        description = "10% off for Bronze"
    } $adminToken
    $benefitId = $benefitCreate.Json.id
    Add-Result "LB01" ($benefitCreate.Status -eq 201 -and $null -ne $benefitId) $benefitCreate.Status "Creating a bronze benefit should return 201"

    $bronzeBenefits = Invoke-Api "GET" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits" $null $adminToken
    $bronzeHasDirectBenefit = @($bronzeBenefits.Json | Where-Object {
            $_.id -eq $benefitId -and $_.inherited -eq $false -and $_.source_tier_name -eq "BRONZE"
        }).Count -eq 1
    Add-Result "LB02" ($bronzeBenefits.Status -eq 200 -and $bronzeHasDirectBenefit) $bronzeBenefits.Status "Bronze benefits should include the newly created direct benefit"

    $silverBenefits = Invoke-Api "GET" $EngagementBaseUrl "/loyalty/tiers/$silverId/benefits" $null $adminToken
    $silverHasInherited = @($silverBenefits.Json | Where-Object {
            $_.inherited -eq $true -and $_.source_tier_name -eq "BRONZE" -and $_.description -eq "10% off for Bronze"
        }).Count -ge 1
    Add-Result "LB03" ($silverBenefits.Status -eq 200 -and $silverHasInherited) $silverBenefits.Status "Silver benefits should include inherited bronze benefit when inheritance is enabled"

    $benefitUpdate = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits/$benefitId" @{
        type = "DISCOUNT"
        value = 15
        description = "15% off for Bronze"
    } $adminToken
    $benefitUpdateOk = $benefitUpdate.Status -eq 200 `
        -and [decimal]$benefitUpdate.Json.value -eq [decimal]"15" `
        -and $benefitUpdate.Json.description -eq "15% off for Bronze"
    Add-Result "LB04" $benefitUpdateOk $benefitUpdate.Status "Updating the bronze benefit should persist new values"

    $invalidBenefitType = Invoke-Api "POST" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits" @{
        type = "INVALID"
        value = 10
        description = "Invalid benefit type"
    } $adminToken
    Add-Result "LB05" ($invalidBenefitType.Status -eq 400 -and (Assert-MessageContains $invalidBenefitType "Invalid benefit type")) $invalidBenefitType.Status "Invalid benefit type should return 400"

    $invalidDiscount = Invoke-Api "POST" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits" @{
        type = "DISCOUNT"
        value = 120
        description = "Too much discount"
    } $adminToken
    Add-Result "LB06" ($invalidDiscount.Status -eq 400 -and (Assert-MessageContains $invalidDiscount "between 0 and 100")) $invalidDiscount.Status "Discount > 100 should return 400"

    $missingTier = Invoke-Api "GET" $EngagementBaseUrl "/loyalty/tiers/999999/benefits" $null $adminToken
    Add-Result "LB07" ($missingTier.Status -eq 404 -and (Assert-MessageContains $missingTier "Tier not found")) $missingTier.Status "Unknown tier should return 404"

    if ($runFull) {
        $validFreeShipping = Invoke-Api "POST" $EngagementBaseUrl "/loyalty/tiers/$silverId/benefits" @{
            type = "FREE_SHIPPING"
            value = 150000
            description = "Free shipping from 150k"
        } $adminToken
        Add-Result "LB07A" ($validFreeShipping.Status -eq 201 -and $validFreeShipping.Json.type -eq "FREE_SHIPPING") $validFreeShipping.Status "FREE_SHIPPING benefit should be created successfully"

        $negativeBenefitValue = Invoke-Api "POST" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits" @{
            type = "BONUS_POINTS"
            value = -1
            description = "Negative bonus"
        } $adminToken
        Add-Result "LB07B" ($negativeBenefitValue.Status -eq 400 -and (Assert-MessageContains $negativeBenefitValue "greater than or equal to 0")) $negativeBenefitValue.Status "Negative non-discount benefit value should return 400"

        $missingBenefitDescription = Invoke-Api "POST" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits" @{
            type = "GIFT"
            value = $null
            description = ""
        } $adminToken
        Add-Result "LB07C" ($missingBenefitDescription.Status -eq 400 -and (Assert-MessageContains $missingBenefitDescription "Description is required")) $missingBenefitDescription.Status "Missing benefit description should return 400"

        $wrongTierUpdate = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/tiers/$silverId/benefits/$benefitId" @{
            type = "DISCOUNT"
            value = 20
            description = "Wrong tier update"
        } $adminToken
        Add-Result "LB07D" ($wrongTierUpdate.Status -eq 404 -and (Assert-MessageContains $wrongTierUpdate "Benefit not found")) $wrongTierUpdate.Status "Updating a benefit through the wrong tier should return 404"

        $unknownBenefitUpdate = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits/999999" @{
            type = "DISCOUNT"
            value = 20
            description = "Unknown benefit"
        } $adminToken
        Add-Result "LB07E" ($unknownBenefitUpdate.Status -eq 404 -and (Assert-MessageContains $unknownBenefitUpdate "Benefit not found")) $unknownBenefitUpdate.Status "Updating an unknown benefit should return 404"

        $configBeforeDisableInheritance = (Invoke-Api "GET" $EngagementBaseUrl "/loyalty/config" $null $adminToken).Json
        $disableInheritanceBody = Build-RestoreTiersBody $configBeforeDisableInheritance
        $disableInheritanceBody.inherit_from_lower_tiers = $false
        $inheritanceDisabled = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" $disableInheritanceBody $adminToken
        $silverWithoutInheritance = Invoke-Api "GET" $EngagementBaseUrl "/loyalty/tiers/$silverId/benefits" $null $adminToken
        $silverInheritedGone = @($silverWithoutInheritance.Json | Where-Object { $_.inherited -eq $true }).Count -eq 0
        Add-Result "LB07F" ($inheritanceDisabled.Status -eq 200 -and (-not $inheritanceDisabled.Json.inherit_from_lower_tiers) -and $silverInheritedGone) $inheritanceDisabled.Status "Disabling inheritance should remove inherited benefits from higher tiers"

        $unknownBenefitDelete = Invoke-Api "DELETE" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits/999999" $null $adminToken
        Add-Result "LB07G" ($unknownBenefitDelete.Status -eq 404 -and (Assert-MessageContains $unknownBenefitDelete "Benefit not found")) $unknownBenefitDelete.Status "Deleting an unknown benefit should return 404"

        $bronzeBenefits = Invoke-Api "GET" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits" $null $adminToken
        $benefitId = Get-BenefitIdByDescription $bronzeBenefits.Json "15% off for Bronze"
    }

    $benefitDelete = Invoke-Api "DELETE" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits/$benefitId" $null $adminToken
    $bronzeAfterDelete = Invoke-Api "GET" $EngagementBaseUrl "/loyalty/tiers/$bronzeId/benefits" $null $adminToken
    $deletedGone = @($bronzeAfterDelete.Json | Where-Object { $_.id -eq $benefitId }).Count -eq 0
    Add-Result "LB08" (($benefitDelete.Status -eq 200) -and ($bronzeAfterDelete.Status -eq 200) -and $deletedGone) $benefitDelete.Status "Deleting the bronze benefit should remove it from the tier"
} catch {
    $fatalError = $_.Exception.Message
    Add-Result "FATAL" $false 0 $fatalError
} finally {
    if (-not $SkipRestore -and $originalConfigCaptured -and $adminToken) {
        try {
            [void](Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/points" (Build-RestorePointsBody $originalConfig) $adminToken)
            [void](Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/expiration" (Build-RestoreExpirationBody $originalConfig) $adminToken)
            $restoreTiers = Invoke-Api "PUT" $EngagementBaseUrl "/loyalty/config/tiers" (Build-RestoreTiersBody $originalConfig) $adminToken
            $cleanupOk = $restoreTiers.Status -eq 200
            $cleanupNote = if ($cleanupOk) {
                "Original loyalty configuration restored"
            } else {
                "Failed to restore original config: $($restoreTiers.Content)"
            }
            Add-Result "CLEANUP" $cleanupOk $restoreTiers.Status $cleanupNote
        } catch {
            Add-Result "CLEANUP" $false 0 ("Failed to restore original config: " + $_.Exception.Message)
        }
    }

    $results | Format-Table -AutoSize

    $failed = @($results | Where-Object { -not $_.Pass }).Count
    if ($fatalError) {
        Write-Host ""
        Write-Host $fatalError -ForegroundColor Red
        exit 1
    }

    if ($failed -gt 0) {
        Write-Host ""
        Write-Host "$failed test(s) failed." -ForegroundColor Red
        exit 1
    }

    Write-Host ""
    Write-Host "All Story 23 + Story 26 backend tests passed ($Mode mode)." -ForegroundColor Green
}
