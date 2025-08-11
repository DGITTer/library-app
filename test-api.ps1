# Library Management API Test Script

# Test base URL
$baseUrl = "http://localhost:8080"

Write-Host "Testing Library Management API..." -ForegroundColor Green

# Test 1: Root endpoint
Write-Host "`n1. Testing root endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/" -Method GET
    Write-Host "✓ Root endpoint works: $response" -ForegroundColor Green
} catch {
    Write-Host "✗ Root endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Register a customer
Write-Host "`n2. Testing customer registration..." -ForegroundColor Yellow
$customerData = @{
    name = "John Doe"
    email = "john.doe@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/customers" -Method POST -Body $customerData -ContentType "application/json"
    Write-Host "✓ Customer registration successful: $($response.name) ($($response.email))" -ForegroundColor Green
} catch {
    Write-Host "✗ Customer registration failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Login
Write-Host "`n3. Testing login..." -ForegroundColor Yellow
$loginData = @{
    email = "john.doe@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/login" -Method POST -Body $loginData -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✓ Login successful, token received" -ForegroundColor Green
} catch {
    Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Test 4: Create a category (authenticated)
Write-Host "`n4. Testing category creation..." -ForegroundColor Yellow
$categoryData = @{
    name = "Science Fiction"
    description = "Books about futuristic and scientific themes"
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Bearer $token"
}

try {
    $categoryResponse = Invoke-RestMethod -Uri "$baseUrl/categories" -Method POST -Body $categoryData -ContentType "application/json" -Headers $headers
    Write-Host "✓ Category created: $($categoryResponse.name)" -ForegroundColor Green
} catch {
    Write-Host "✗ Category creation failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Get all categories (public)
Write-Host "`n5. Testing get all categories..." -ForegroundColor Yellow
try {
    $categories = Invoke-RestMethod -Uri "$baseUrl/categories" -Method GET
    Write-Host "✓ Categories retrieved: $($categories.Count) categories found" -ForegroundColor Green
} catch {
    Write-Host "✗ Get categories failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Create a book (authenticated)
Write-Host "`n6. Testing book creation..." -ForegroundColor Yellow
$bookData = @{
    title = "The Hitchhiker's Guide to the Galaxy"
    author = "Douglas Adams"
    publisher = "Pan Books"
    publishingYear = 1979
    categoryId = 1
} | ConvertTo-Json

try {
    $bookResponse = Invoke-RestMethod -Uri "$baseUrl/books" -Method POST -Body $bookData -ContentType "application/json" -Headers $headers
    Write-Host "✓ Book created: $($bookResponse.title) by $($bookResponse.author)" -ForegroundColor Green
} catch {
    Write-Host "✗ Book creation failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: Get all books (public)
Write-Host "`n7. Testing get all books..." -ForegroundColor Yellow
try {
    $books = Invoke-RestMethod -Uri "$baseUrl/books" -Method GET
    Write-Host "✓ Books retrieved: $($books.Count) books found" -ForegroundColor Green
} catch {
    Write-Host "✗ Get books failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 8: Try unauthorized access
Write-Host "`n8. Testing unauthorized access..." -ForegroundColor Yellow
try {
    $unauthorizedResponse = Invoke-RestMethod -Uri "$baseUrl/categories" -Method POST -Body $categoryData -ContentType "application/json"
    Write-Host "✗ Unauthorized access succeeded (should have failed)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✓ Unauthorized access properly blocked" -ForegroundColor Green
    } else {
        Write-Host "✗ Unexpected error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== API Testing Complete ===" -ForegroundColor Green
