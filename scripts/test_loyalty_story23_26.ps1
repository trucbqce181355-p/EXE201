[CmdletBinding()]
param(
    [string]$AuthBaseUrl = "http://localhost:8081",
    [string]$EngagementBaseUrl = "http://localhost:8083",
    [string]$AdminEmail = "admin@gmail.com",
    [string]$AdminPassword = "123456",
    [string]$NegativeEmail = "manager@gmail.com",
    [string]$NegativePassword = "123456"
)

$ErrorActionPreference = "Stop"

$checks = New-Object System.Collections.Generic.List[object]
$createdBenefitId = $null
$createdBenefitTierId = $null
$restoreInheritanceNeeded = $false
$originalConfig = $null

function Add-Check {
    param(
        [string]$Name,
        [bool]$Passed,
        [string]$Detail
    )

    $checks.Add([pscustomobject]@{
        Name = $Name
        Passed = $Passed
        Detail = $Detail
    }) | Out-Null
}

function Convert-ApiBody {
    param([string]$Raw)

    if ([string]::IsNullOrWhiteSpace($Raw)) {
        return $null
    }

    try {
        return $Raw | ConvertFrom-Json
    } catch {
        return $Raw
    }
}

function Invoke-Api {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Uri,
        [string]$Token,
        $Body
    )

    $headers = @{
        Accept = "application/json"
    }

    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $params = @{
        Method = $Method
        Uri = $Uri
        Headers = $headers
        UseBasicParsing = $true
    }

    if ($null -ne $Body) {
        $params["ContentType"] = "application/json"
        $params["Body"] = $Body | ConvertTo-Json -Depth 20
    }

    try {
        $response = Invoke-WebRequest @params
        return [pscustomobject]@{
            StatusCode = [int]$response.StatusCode
            Body = Convert-ApiBody $response.Content
            Raw = $response.Content
        }
    } catch {
        if (-not $_.Exception.Response) {
            throw
        }

        $response = $_.Exception.Response
        $stream = $response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        try {
            $raw = $reader.ReadToEnd()
        } finally {
            $reader.Dispose()
            $stream.Dispose()
        }

        return [pscustomobject]@{
            StatusCode = [int]$response.StatusCode
            Body = Convert-ApiBody $raw
            Raw = $raw
        }
    }
}

function Assert-Status {
    param(
        [string]$Name,
        [int]$Expected,
        $Response,
        [string]$Detail
    )

    $passed = $Response.StatusCode -eq $Expected
    $finalDetail = if ($passed) {
        $Detail
    } else {
        "$Detail (expected $Expected, got $($Response.StatusCode))"
    }

    Add-Check -Name $Name -Passed $passed -Detail $finalDetail

    if (-not $passed) {
        throw "Check failed: $Name"
    }
}

function Assert-Condition {
    param(
        [string]$Name,
        [bool]$Condition,
        [string]$Detail
    )

    Add-Check -Name $Name -Passed $Condition -Detail $Detail
    if (-not $Condition) {
        throw "Check failed: $Name"
    }
}

function Get-Message {
    param($Body)

    if ($null -eq $Body) {
        return ""
    }

    if ($Body -is [string]) {
        return $Body
    }

    if ($Body.PSObject.Properties.Name -contains "message") {
        return [string]$Body.message
    }

    return ($Body | ConvertTo-Json -Depth 10 -Compress)
}

function New-PointPayload {
    param($Config)

    return @{
        points_per_currency = [decimal]$Config.points_per_currency
        min_order_amount = [decimal]$Config.min_order_amount
        excluded_categories = @($Config.excluded_categories)
    }
}

function New-ExpirationPayload {
    param($Config)

    return @{
        expiration_months = [int]$Config.expiration_months
        evaluation_period_months = [int]$Config.evaluation_period_months
    }
}

function New-TierPayload {
    param(
        $Config,
        [Nullable[bool]]$InheritOverride = $null
    )

    $inheritValue = if ($null -ne $InheritOverride) {
        [bool]$InheritOverride
    } else {
        [bool]$Config.inherit_from_lower_tiers
    }

    $tiers = @()
    foreach ($tier in $Config.tiers) {
        $benefits = @()
        foreach ($benefit in @($tier.benefits)) {
            if ($benefit.inherited -eq $true) {
                continue
            }

            $benefitPayload = @{
                type = [string]$benefit.type
                description = [string]$benefit.description
            }

            if ($null -ne $benefit.value) {
                $benefitPayload["value"] = [decimal]$benefit.value
            } else {
                $benefitPayload["value"] = $null
            }

            if ($null -ne $benefit.id) {
                $benefitPayload["id"] = [int64]$benefit.id
            }

            $benefits += $benefitPayload
        }

        $tierPayload = @{
            id = [int64]$tier.id
            name = [string]$tier.name
            min_points = [int]$tier.min_points
            benefits = $benefits
        }

        if ($null -ne $tier.max_points) {
            $tierPayload["max_points"] = [int]$tier.max_points
        } else {
            $tierPayload["max_points"] = $null
        }

        $tiers += $tierPayload
    }

    return @{
        inherit_from_lower_tiers = $inheritValue
        tiers = $tiers
    }
}

try {
    Write-Host ""
    Write-Host "Smoke testing Story 23 + Story 26 against:" -ForegroundColor Cyan
    Write-Host "  Auth: $AuthBaseUrl" -ForegroundColor DarkGray
    Write-Host "  Engagement: $EngagementBaseUrl" -ForegroundColor DarkGray
    Write-Host ""

    $adminLogin = Invoke-Api -Method "POST" -Uri "$AuthBaseUrl/api/auth/login" -Body @{
        email = $AdminEmail
        password = $AdminPassword
    }
    Assert-Status -Name "Login as admin" -Expected 200 -Response $adminLogin -Detail "Admin account can log in"

    $adminToken = $adminLogin.Body.accessToken
    Assert-Condition -Name "Admin access token issued" -Condition (-not [string]::IsNullOrWhiteSpace($adminToken)) -Detail "Auth service returned access token"

    $anonymousConfig = Invoke-Api -Method "GET" -Uri "$EngagementBaseUrl/loyalty/config"
    Assert-Status -Name "GET /loyalty/config requires auth" -Expected 401 -Response $anonymousConfig -Detail "Anonymous request is rejected"

    $negativeLogin = Invoke-Api -Method "POST" -Uri "$AuthBaseUrl/api/auth/login" -Body @{
        email = $NegativeEmail
        password = $NegativePassword
    }
    Assert-Status -Name "Login as non-loyalty user" -Expected 200 -Response $negativeLogin -Detail "Negative-role account can log in"

    $negativeConfig = Invoke-Api -Method "GET" -Uri "$EngagementBaseUrl/loyalty/config" -Token $negativeLogin.Body.accessToken
    Assert-Status -Name "LOYALTY:CONFIG is enforced" -Expected 403 -Response $negativeConfig -Detail "$NegativeEmail is blocked from config API"

    $configResponse = Invoke-Api -Method "GET" -Uri "$EngagementBaseUrl/loyalty/config" -Token $adminToken
    Assert-Status -Name "GET /loyalty/config works" -Expected 200 -Response $configResponse -Detail "Admin can read current loyalty config"

    $config = $configResponse.Body
    $originalConfig = $config
    Assert-Condition -Name "Config contains tiers" -Condition (@($config.tiers).Count -ge 1) -Detail ("Returned {0} tiers" -f @($config.tiers).Count)

    $pointSave = Invoke-Api -Method "PUT" -Uri "$EngagementBaseUrl/loyalty/config/points" -Token $adminToken -Body (New-PointPayload -Config $config)
    Assert-Status -Name "PUT /loyalty/config/points works" -Expected 200 -Response $pointSave -Detail "Current point rules save successfully"

    $pointInvalid = Invoke-Api -Method "PUT" -Uri "$EngagementBaseUrl/loyalty/config/points" -Token $adminToken -Body @{
        points_per_currency = 0
        min_order_amount = [decimal]$config.min_order_amount
        excluded_categories = @($config.excluded_categories)
    }
    Assert-Status -Name "points_per_currency > 0 validation" -Expected 400 -Response $pointInvalid -Detail (Get-Message $pointInvalid.Body)

    $expirationSave = Invoke-Api -Method "PUT" -Uri "$EngagementBaseUrl/loyalty/config/expiration" -Token $adminToken -Body (New-ExpirationPayload -Config $config)
    Assert-Status -Name "PUT /loyalty/config/expiration works" -Expected 200 -Response $expirationSave -Detail "Current expiration rules save successfully"

    $expirationInvalid = Invoke-Api -Method "PUT" -Uri "$EngagementBaseUrl/loyalty/config/expiration" -Token $adminToken -Body @{
        expiration_months = 0
        evaluation_period_months = [int]$config.evaluation_period_months
    }
    Assert-Status -Name "expiration_months >= 1 validation" -Expected 400 -Response $expirationInvalid -Detail (Get-Message $expirationInvalid.Body)

    $tierPayload = New-TierPayload -Config $config
    $tierSave = Invoke-Api -Method "PUT" -Uri "$EngagementBaseUrl/loyalty/config/tiers" -Token $adminToken -Body $tierPayload
    Assert-Status -Name "PUT /loyalty/config/tiers works" -Expected 200 -Response $tierSave -Detail "Current tier configuration saves successfully"

    if (@($tierPayload.tiers).Count -ge 2) {
        $invalidTierPayload = New-TierPayload -Config $config
        $invalidTierPayload.tiers[1].min_points = [int]$invalidTierPayload.tiers[0].max_points
        $tierInvalid = Invoke-Api -Method "PUT" -Uri "$EngagementBaseUrl/loyalty/config/tiers" -Token $adminToken -Body $invalidTierPayload
        Assert-Status -Name "Tier continuity validation" -Expected 400 -Response $tierInvalid -Detail (Get-Message $tierInvalid.Body)
    } else {
        Add-Check -Name "Tier continuity validation" -Passed $true -Detail "Skipped because fewer than 2 tiers exist"
    }

    $bronzeTier = @($config.tiers | Sort-Object min_points)[0]
    Assert-Condition -Name "Tier selected for benefits" -Condition ($null -ne $bronzeTier.id) -Detail ("Using tier {0} (id {1})" -f $bronzeTier.name, $bronzeTier.id)

    $benefitsUnauthorized = Invoke-Api -Method "GET" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits"
    Assert-Status -Name "GET benefits requires auth" -Expected 401 -Response $benefitsUnauthorized -Detail "Anonymous request is rejected"

    $benefitsForbidden = Invoke-Api -Method "GET" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits" -Token $negativeLogin.Body.accessToken
    Assert-Status -Name "LOYALTY:MANAGE_BENEFITS is enforced" -Expected 403 -Response $benefitsForbidden -Detail "$NegativeEmail is blocked from tier benefits API"

    $benefitsGet = Invoke-Api -Method "GET" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits" -Token $adminToken
    Assert-Status -Name "GET /loyalty/tiers/{id}/benefits works" -Expected 200 -Response $benefitsGet -Detail "Admin can read tier benefits"

    $staleSmokeBenefits = @($benefitsGet.Body | Where-Object {
        $_.inherited -eq $false -and ($_.description -like "Story26 smoke *" -or $_.description -eq "debug delete check")
    })
    foreach ($staleBenefit in $staleSmokeBenefits) {
        $staleDelete = Invoke-Api -Method "DELETE" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits/$($staleBenefit.id)" -Token $adminToken
        Assert-Status -Name ("Cleanup stale benefit {0}" -f $staleBenefit.id) -Expected 200 -Response $staleDelete -Detail "Removed leftover smoke-test benefit"
    }
    if ($staleSmokeBenefits.Count -gt 0) {
        $benefitsGet = Invoke-Api -Method "GET" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits" -Token $adminToken
        Assert-Status -Name "Refresh benefits after cleanup" -Expected 200 -Response $benefitsGet -Detail "Benefit list refreshed after stale cleanup"
    }

    $invalidType = Invoke-Api -Method "POST" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits" -Token $adminToken -Body @{
        type = "INVALID_KIND"
        value = 10
        description = "Invalid benefit type check"
    }
    Assert-Status -Name "Invalid benefit type validation" -Expected 400 -Response $invalidType -Detail (Get-Message $invalidType.Body)

    $invalidDiscount = Invoke-Api -Method "POST" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits" -Token $adminToken -Body @{
        type = "DISCOUNT"
        value = 101
        description = "Discount over 100 check"
    }
    Assert-Status -Name "Discount 0-100 validation" -Expected 400 -Response $invalidDiscount -Detail (Get-Message $invalidDiscount.Body)

    $tempDescription = "Story26 smoke $(Get-Date -Format 'yyyyMMddHHmmss')"
    $restoreConfigNeeded = -not [bool]$config.inherit_from_lower_tiers

    if ($restoreConfigNeeded) {
        $enableInheritance = Invoke-Api -Method "PUT" -Uri "$EngagementBaseUrl/loyalty/config/tiers" -Token $adminToken -Body (New-TierPayload -Config $config -InheritOverride $true)
        Assert-Status -Name "Enable inheritance for cascading test" -Expected 200 -Response $enableInheritance -Detail "Temporarily enabled inherit_from_lower_tiers"
        $restoreInheritanceNeeded = $true
    }

    $createBenefit = Invoke-Api -Method "POST" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits" -Token $adminToken -Body @{
        type = "DISCOUNT"
        value = 10
        description = $tempDescription
    }
    Assert-Status -Name "POST /loyalty/tiers/{id}/benefits works" -Expected 201 -Response $createBenefit -Detail "Temporary benefit created"

    $createdBenefitId = $createBenefit.Body.id
    $createdBenefitTierId = $bronzeTier.id
    Assert-Condition -Name "Created benefit id returned" -Condition ($null -ne $createdBenefitId) -Detail ("Created benefit id = {0}" -f $createdBenefitId)

    $updateDescription = "$tempDescription updated"
    $updateBenefit = Invoke-Api -Method "PUT" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits/$createdBenefitId" -Token $adminToken -Body @{
        type = "DISCOUNT"
        value = 15
        description = $updateDescription
    }
    Assert-Status -Name "PUT /loyalty/tiers/{id}/benefits/{benefitId} works" -Expected 200 -Response $updateBenefit -Detail "Temporary benefit updated"
    Assert-Condition -Name "Benefit update persisted" -Condition ($updateBenefit.Body.value -eq 15 -and $updateBenefit.Body.description -eq $updateDescription) -Detail "Updated value and description are returned"

    $directBenefits = Invoke-Api -Method "GET" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits" -Token $adminToken
    Assert-Status -Name "Created benefit appears in GET list" -Expected 200 -Response $directBenefits -Detail "Benefit list refreshed"
    $directHit = @($directBenefits.Body | Where-Object { $_.id -eq $createdBenefitId -and $_.inherited -eq $false })
    Assert-Condition -Name "Direct benefit is listed" -Condition ($directHit.Count -eq 1) -Detail "Created benefit is present as direct benefit"

    $nextTier = @($config.tiers | Sort-Object min_points)[1]
    if ($null -ne $nextTier) {
        $inheritedBenefits = Invoke-Api -Method "GET" -Uri "$EngagementBaseUrl/loyalty/tiers/$($nextTier.id)/benefits" -Token $adminToken
        Assert-Status -Name "Benefit cascading lookup works" -Expected 200 -Response $inheritedBenefits -Detail "Higher tier benefits can be queried"

        $inheritedHit = @($inheritedBenefits.Body | Where-Object {
            $_.description -eq $updateDescription -and $_.inherited -eq $true -and $_.source_tier_id -eq $bronzeTier.id
        })
        Assert-Condition -Name "Higher tier inherits lower-tier benefit" -Condition ($inheritedHit.Count -ge 1) -Detail "Cascading benefit is visible from next tier"
    }

    $deleteBenefit = Invoke-Api -Method "DELETE" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits/$createdBenefitId" -Token $adminToken
    Assert-Status -Name "DELETE /loyalty/tiers/{id}/benefits/{benefitId} works" -Expected 200 -Response $deleteBenefit -Detail "Temporary benefit deleted"

    $afterDelete = Invoke-Api -Method "GET" -Uri "$EngagementBaseUrl/loyalty/tiers/$($bronzeTier.id)/benefits" -Token $adminToken
    Assert-Status -Name "Deleted benefit is removed from list" -Expected 200 -Response $afterDelete -Detail "Benefit list refreshed after delete"
    $afterDeleteHit = @($afterDelete.Body | Where-Object { $_.id -eq $createdBenefitId })
    Assert-Condition -Name "Deleted benefit no longer exists" -Condition ($afterDeleteHit.Count -eq 0) -Detail "Temporary benefit is gone"

    if ($restoreConfigNeeded) {
        $restoreResponse = Invoke-Api -Method "PUT" -Uri "$EngagementBaseUrl/loyalty/config/tiers" -Token $adminToken -Body (New-TierPayload -Config $config -InheritOverride ([bool]$config.inherit_from_lower_tiers))
        Assert-Status -Name "Restore original inheritance flag" -Expected 200 -Response $restoreResponse -Detail "inherit_from_lower_tiers restored to original value"
        $restoreInheritanceNeeded = $false
    }
} catch {
    Write-Host ""
    Write-Host ("Smoke test stopped: {0}" -f $_.Exception.Message) -ForegroundColor Red
} finally {
    if ($null -ne $createdBenefitId -and $null -ne $createdBenefitTierId) {
        try {
            Invoke-Api -Method "DELETE" -Uri "$EngagementBaseUrl/loyalty/tiers/$createdBenefitTierId/benefits/$createdBenefitId" -Token $adminToken | Out-Null
        } catch {
            Add-Check -Name "Cleanup benefit" -Passed $false -Detail $_.Exception.Message
        }
    }

    if ($restoreInheritanceNeeded -and $null -ne $originalConfig) {
        try {
            Invoke-Api -Method "PUT" -Uri "$EngagementBaseUrl/loyalty/config/tiers" -Token $adminToken -Body (New-TierPayload -Config $originalConfig -InheritOverride ([bool]$originalConfig.inherit_from_lower_tiers)) | Out-Null
        } catch {
            Add-Check -Name "Restore inheritance in cleanup" -Passed $false -Detail $_.Exception.Message
        }
    }

    Write-Host ""
    Write-Host "Results" -ForegroundColor Cyan
    foreach ($check in $checks) {
        $prefix = if ($check.Passed) { "[PASS]" } else { "[FAIL]" }
        $color = if ($check.Passed) { "Green" } else { "Red" }
        Write-Host ("{0} {1} - {2}" -f $prefix, $check.Name, $check.Detail) -ForegroundColor $color
    }

    $failed = @($checks | Where-Object { -not $_.Passed })
    Write-Host ""
    if ($failed.Count -eq 0 -and $checks.Count -gt 0) {
        Write-Host ("Story 23 + 26 smoke test passed: {0} checks" -f $checks.Count) -ForegroundColor Green
        exit 0
    }

    Write-Host ("Story 23 + 26 smoke test failed: {0} of {1} checks failed" -f $failed.Count, $checks.Count) -ForegroundColor Red
    exit 1
}
