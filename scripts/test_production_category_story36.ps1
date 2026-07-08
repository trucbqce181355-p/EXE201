[CmdletBinding()]
param(
    [string]$AuthBaseUrl = "http://localhost:8081",
    [string]$ProductionBaseUrl = "http://localhost:8084",
    [string]$AdminEmail = "admin@gmail.com",
    [string]$AdminPassword = "123456",
    [string]$ReadOnlyEmail = "manager@gmail.com",
    [string]$ReadOnlyPassword = "123456"
)

$ErrorActionPreference = "Stop"

$checks = New-Object System.Collections.Generic.List[object]
$createdRootId = $null
$createdChildId = $null
$createdInactiveId = $null
$createdProductCategoryId = $null
$createdProductId = $null
$adminToken = $null

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

    $headers = @{ Accept = "application/json" }
    if ($Token) {
        $headers.Authorization = "Bearer $Token"
    }

    $params = @{
        Method = $Method
        Uri = $Uri
        Headers = $headers
        UseBasicParsing = $true
    }

    if ($null -ne $Body) {
        $params.ContentType = "application/json"
        $params.Body = $Body | ConvertTo-Json -Depth 20
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
    Add-Check -Name $Name -Passed $passed -Detail ($(if ($passed) { $Detail } else { "$Detail (expected $Expected, got $($Response.StatusCode))" }))
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

function Get-ApiData {
    param($Response)

    if ($null -eq $Response -or $null -eq $Response.Body) {
        return $null
    }

    if ($Response.Body -is [string]) {
        return $null
    }

    if ($Response.Body.PSObject.Properties.Name -contains "data") {
        return $Response.Body.data
    }

    return $Response.Body
}

function Get-Message {
    param($Response)

    if ($null -eq $Response -or $null -eq $Response.Body) {
        return ""
    }

    if ($Response.Body -is [string]) {
        return $Response.Body
    }

    if ($Response.Body.PSObject.Properties.Name -contains "message") {
        return [string]$Response.Body.message
    }

    return ($Response.Body | ConvertTo-Json -Depth 10 -Compress)
}

function Remove-CategorySafe {
    param([long]$CategoryId)

    if (-not $adminToken) {
        return
    }

    Invoke-Api -Method "DELETE" -Uri "$ProductionBaseUrl/categories/$CategoryId" -Token $adminToken | Out-Null
}

function Remove-ProductSafe {
    param([long]$ProductId)

    if (-not $adminToken) {
        return
    }

    Invoke-Api -Method "DELETE" -Uri "$ProductionBaseUrl/products/$ProductId" -Token $adminToken | Out-Null
}

try {
    $suffix = Get-Date -Format "yyyyMMddHHmmss"

    Write-Host ""
    Write-Host "Smoke testing Story 36 against:" -ForegroundColor Cyan
    Write-Host "  Auth: $AuthBaseUrl" -ForegroundColor DarkGray
    Write-Host "  Production: $ProductionBaseUrl" -ForegroundColor DarkGray
    Write-Host ""

    $adminLogin = Invoke-Api -Method "POST" -Uri "$AuthBaseUrl/api/auth/login" -Body @{
        email = $AdminEmail
        password = $AdminPassword
    }
    Assert-Status -Name "Login as admin" -Expected 200 -Response $adminLogin -Detail "Admin account can log in"
    $adminToken = (Get-ApiData $adminLogin).accessToken
    Assert-Condition -Name "Admin access token issued" -Condition (-not [string]::IsNullOrWhiteSpace($adminToken)) -Detail "Auth service returned access token"

    $readOnlyLogin = Invoke-Api -Method "POST" -Uri "$AuthBaseUrl/api/auth/login" -Body @{
        email = $ReadOnlyEmail
        password = $ReadOnlyPassword
    }
    Assert-Status -Name "Login as read-only manager" -Expected 200 -Response $readOnlyLogin -Detail "Read-only account can log in"
    $readOnlyToken = (Get-ApiData $readOnlyLogin).accessToken

    $anonymousList = Invoke-Api -Method "GET" -Uri "$ProductionBaseUrl/categories"
    Assert-Status -Name "GET /categories requires auth" -Expected 401 -Response $anonymousList -Detail "Anonymous request is rejected"

    $managerRead = Invoke-Api -Method "GET" -Uri "$ProductionBaseUrl/categories" -Token $readOnlyToken
    Assert-Status -Name "CATEGORY:READ works" -Expected 200 -Response $managerRead -Detail "$ReadOnlyEmail can read categories"

    $managerCreate = Invoke-Api -Method "POST" -Uri "$ProductionBaseUrl/categories" -Token $readOnlyToken -Body @{
        name = "Forbidden category"
        slug = "forbidden-$suffix"
    }
    Assert-Status -Name "CATEGORY:CREATE is enforced" -Expected 403 -Response $managerCreate -Detail "$ReadOnlyEmail cannot create categories"

    $createRoot = Invoke-Api -Method "POST" -Uri "$ProductionBaseUrl/categories" -Token $adminToken -Body @{
        name = "Smoke Root $suffix"
        slug = "smoke-root-$suffix"
        description = "Temporary root category for story 36 smoke test"
        image_url = "https://example.com/category-root.png"
        display_order = 101
        active = $true
    }
    Assert-Status -Name "POST /categories creates root category" -Expected 201 -Response $createRoot -Detail "Root category created"
    $createdRootId = (Get-ApiData $createRoot).id
    Assert-Condition -Name "Root category id returned" -Condition ($null -ne $createdRootId) -Detail ("Created root category id = {0}" -f $createdRootId)

    $createChild = Invoke-Api -Method "POST" -Uri "$ProductionBaseUrl/categories" -Token $adminToken -Body @{
        name = "Smoke Child $suffix"
        slug = "smoke-child-$suffix"
        parent_id = $createdRootId
        display_order = 5
        active = $true
    }
    Assert-Status -Name "POST /categories creates subcategory" -Expected 201 -Response $createChild -Detail "Child category created"
    $createdChildId = (Get-ApiData $createChild).id

    $createInactive = Invoke-Api -Method "POST" -Uri "$ProductionBaseUrl/categories" -Token $adminToken -Body @{
        name = "Smoke Inactive $suffix"
        slug = "smoke-inactive-$suffix"
        active = $false
    }
    Assert-Status -Name "POST /categories creates inactive category" -Expected 201 -Response $createInactive -Detail "Inactive category created"
    $createdInactiveId = (Get-ApiData $createInactive).id

    $invalidParent = Invoke-Api -Method "POST" -Uri "$ProductionBaseUrl/categories" -Token $adminToken -Body @{
        name = "Broken Parent $suffix"
        slug = "broken-parent-$suffix"
        parent_id = 999999999
    }
    Assert-Status -Name "Parent category must exist" -Expected 400 -Response $invalidParent -Detail (Get-Message $invalidParent)

    $duplicateSlug = Invoke-Api -Method "POST" -Uri "$ProductionBaseUrl/categories" -Token $adminToken -Body @{
        name = "Duplicate Slug $suffix"
        slug = "smoke-root-$suffix"
    }
    Assert-Status -Name "Category slug must be unique" -Expected 409 -Response $duplicateSlug -Detail (Get-Message $duplicateSlug)

    $rootList = Invoke-Api -Method "GET" -Uri "$ProductionBaseUrl/categories?parent_id=null" -Token $adminToken
    Assert-Status -Name "GET /categories?parent_id=null works" -Expected 200 -Response $rootList -Detail "Root category filter works"
    $rootListData = @(Get-ApiData $rootList)
    Assert-Condition -Name "Root category appears in root list" -Condition (@($rootListData | Where-Object { $_.id -eq $createdRootId }).Count -eq 1) -Detail "Created root category is listed at root level"
    Assert-Condition -Name "Inactive category appears without active filter" -Condition (@($rootListData | Where-Object { $_.id -eq $createdInactiveId }).Count -eq 1) -Detail "Inactive category is listed when no active filter is applied"

    $activeRootList = Invoke-Api -Method "GET" -Uri "$ProductionBaseUrl/categories?parent_id=null&is_active=true" -Token $adminToken
    Assert-Status -Name "GET /categories with is_active=true works" -Expected 200 -Response $activeRootList -Detail "Active filter works"
    $activeRootData = @(Get-ApiData $activeRootList)
    Assert-Condition -Name "Active filter keeps active root" -Condition (@($activeRootData | Where-Object { $_.id -eq $createdRootId }).Count -eq 1) -Detail "Active root category remains visible"
    Assert-Condition -Name "Active filter hides inactive category" -Condition (@($activeRootData | Where-Object { $_.id -eq $createdInactiveId }).Count -eq 0) -Detail "Inactive category is hidden"

    $childList = Invoke-Api -Method "GET" -Uri "$ProductionBaseUrl/categories?parent_id=$createdRootId" -Token $adminToken
    Assert-Status -Name "GET /categories by parent id works" -Expected 200 -Response $childList -Detail "Child filter works"
    $childListData = @(Get-ApiData $childList)
    Assert-Condition -Name "Child category appears in parent filter" -Condition (@($childListData | Where-Object { $_.id -eq $createdChildId }).Count -eq 1) -Detail "Created child category is returned by parent filter"

    $rootDetail = Invoke-Api -Method "GET" -Uri "$ProductionBaseUrl/categories/$createdRootId" -Token $adminToken
    Assert-Status -Name "GET /categories/{id} works" -Expected 200 -Response $rootDetail -Detail "Category detail can be fetched"
    $rootDetailData = Get-ApiData $rootDetail
    Assert-Condition -Name "Category detail includes subcategories" -Condition (@($rootDetailData.subcategories | Where-Object { $_.id -eq $createdChildId }).Count -eq 1) -Detail "Root detail includes child category"

    $updateChild = Invoke-Api -Method "PUT" -Uri "$ProductionBaseUrl/categories/$createdChildId" -Token $adminToken -Body @{
        description = "Updated child description"
        image_url = "https://example.com/category-child-updated.png"
    }
    Assert-Status -Name "PUT /categories/{id} works" -Expected 200 -Response $updateChild -Detail "Child category updated"
    $updateChildData = Get-ApiData $updateChild
    Assert-Condition -Name "Update keeps parent when parent_id is omitted" -Condition ($updateChildData.parent_id -eq $createdRootId) -Detail "Child category still belongs to the same parent"

    $blankNameUpdate = Invoke-Api -Method "PUT" -Uri "$ProductionBaseUrl/categories/$createdRootId" -Token $adminToken -Body @{
        name = "   "
    }
    Assert-Status -Name "Blank category name is rejected on update" -Expected 400 -Response $blankNameUpdate -Detail (Get-Message $blankNameUpdate)

    $createProductCategory = Invoke-Api -Method "POST" -Uri "$ProductionBaseUrl/categories" -Token $adminToken -Body @{
        name = "Smoke Product Category $suffix"
        slug = "smoke-product-category-$suffix"
        active = $true
    }
    Assert-Status -Name "Create category for product constraint" -Expected 201 -Response $createProductCategory -Detail "Product-holder category created"
    $createdProductCategoryId = (Get-ApiData $createProductCategory).id

    $createProduct = Invoke-Api -Method "POST" -Uri "$ProductionBaseUrl/products" -Token $adminToken -Body @{
        name = "Smoke Product $suffix"
        sku = "SMOKE-SKU-$suffix"
        price = 45000
        categoryId = $createdProductCategoryId
        description = "Temporary product for category smoke test"
        available = $true
        preparationTime = 5
    }
    Assert-Status -Name "Create temporary product for category constraints" -Expected 201 -Response $createProduct -Detail "Temporary product created"
    $createdProductId = (Get-ApiData $createProduct).id

    $productCategoryDetail = Invoke-Api -Method "GET" -Uri "$ProductionBaseUrl/categories/$createdProductCategoryId" -Token $adminToken
    Assert-Status -Name "Category detail includes product count" -Expected 200 -Response $productCategoryDetail -Detail "Product-holder category detail can be fetched"
    Assert-Condition -Name "Category detail returns product_count" -Condition ((Get-ApiData $productCategoryDetail).product_count -ge 1) -Detail "Product count reflects linked product"

    $slugUpdateBlocked = Invoke-Api -Method "PUT" -Uri "$ProductionBaseUrl/categories/$createdProductCategoryId" -Token $adminToken -Body @{
        slug = "smoke-product-category-updated-$suffix"
    }
    Assert-Status -Name "Cannot change slug when products are linked" -Expected 400 -Response $slugUpdateBlocked -Detail (Get-Message $slugUpdateBlocked)

    $deleteRootBlocked = Invoke-Api -Method "DELETE" -Uri "$ProductionBaseUrl/categories/$createdRootId" -Token $adminToken
    Assert-Status -Name "Cannot delete category with subcategories" -Expected 400 -Response $deleteRootBlocked -Detail (Get-Message $deleteRootBlocked)

    $deleteProductCategoryBlocked = Invoke-Api -Method "DELETE" -Uri "$ProductionBaseUrl/categories/$createdProductCategoryId" -Token $adminToken
    Assert-Status -Name "Cannot delete category with products" -Expected 400 -Response $deleteProductCategoryBlocked -Detail (Get-Message $deleteProductCategoryBlocked)

    $deleteProduct = Invoke-Api -Method "DELETE" -Uri "$ProductionBaseUrl/products/$createdProductId" -Token $adminToken
    Assert-Status -Name "Temporary product can be deleted" -Expected 200 -Response $deleteProduct -Detail "Temporary product deleted"
    $createdProductId = $null

    $deleteChild = Invoke-Api -Method "DELETE" -Uri "$ProductionBaseUrl/categories/$createdChildId" -Token $adminToken
    Assert-Status -Name "Leaf category can be deleted" -Expected 200 -Response $deleteChild -Detail "Child category deleted"
    $createdChildId = $null

    $deleteRoot = Invoke-Api -Method "DELETE" -Uri "$ProductionBaseUrl/categories/$createdRootId" -Token $adminToken
    Assert-Status -Name "Root category can be deleted after child removal" -Expected 200 -Response $deleteRoot -Detail "Root category deleted"
    $createdRootId = $null

    $deleteInactive = Invoke-Api -Method "DELETE" -Uri "$ProductionBaseUrl/categories/$createdInactiveId" -Token $adminToken
    Assert-Status -Name "Inactive category can be deleted" -Expected 200 -Response $deleteInactive -Detail "Inactive category deleted"
    $createdInactiveId = $null

    $deleteProductCategory = Invoke-Api -Method "DELETE" -Uri "$ProductionBaseUrl/categories/$createdProductCategoryId" -Token $adminToken
    Assert-Status -Name "Product-holder category can be deleted after product removal" -Expected 200 -Response $deleteProductCategory -Detail "Category deleted after product cleanup"
    $createdProductCategoryId = $null
} catch {
    Write-Host ""
    Write-Host ("Smoke test stopped: {0}" -f $_.Exception.Message) -ForegroundColor Red
} finally {
    if ($null -ne $createdProductId) {
        try {
            Remove-ProductSafe -ProductId $createdProductId
        } catch {
            Add-Check -Name "Cleanup product" -Passed $false -Detail $_.Exception.Message
        }
    }

    foreach ($categoryId in @($createdChildId, $createdRootId, $createdInactiveId, $createdProductCategoryId)) {
        if ($null -eq $categoryId) {
            continue
        }

        try {
            Remove-CategorySafe -CategoryId $categoryId
        } catch {
            Add-Check -Name ("Cleanup category {0}" -f $categoryId) -Passed $false -Detail $_.Exception.Message
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
        Write-Host ("Story 36 smoke test passed: {0} checks" -f $checks.Count) -ForegroundColor Green
        exit 0
    }

    Write-Host ("Story 36 smoke test failed: {0} of {1} checks failed" -f $failed.Count, $checks.Count) -ForegroundColor Red
    exit 1
}
