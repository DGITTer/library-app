package com.example

import com.example.models.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.server.config.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class ApplicationTest {
    
    private fun ApplicationTestBuilder.configureTestClient() = createClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    
    private fun ApplicationTestBuilder.configureTestEnvironment() {
        environment {
            config = MapApplicationConfig().apply {
                // Ktor configuration
                put("ktor.deployment.port", "8080")
                put("ktor.testing", "true")
                
                // JWT configuration
                put("jwt.audience", "jwt-audience")
                put("jwt.domain", "http://localhost:8080/")
                put("jwt.realm", "library-app")
                put("jwt.secret", "secret")
                
                // Postgres configuration (will use embedded H2 when ktor.testing = true)
                put("postgres.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
                put("postgres.user", "root")
                put("postgres.password", "")
            }
        }
    }

    @Test
    fun testRoot() = testApplication {
        configureTestEnvironment()
        application {
            module()
        }
        val client = configureTestClient()
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Library Management API", response.bodyAsText())
    }

    @Test
    fun testCustomerRegistration() = testApplication {
        configureTestEnvironment()
        application {
            module()
        }
        val client = configureTestClient()
        
        val customerCreate = CustomerCreate(
            name = "John Doe",
            email = "john.doe@example.com",
            password = "password123"
        )
        
        val response = client.post("/customers") {
            contentType(ContentType.Application.Json)
            setBody(customerCreate)
        }
        
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun testCustomerRegistrationWithInvalidEmail() = testApplication {
        configureTestEnvironment()
        application {
            module()
        }
        val client = configureTestClient()
        
        val customerCreate = CustomerCreate(
            name = "John Doe",
            email = "invalid-email",
            password = "password123"
        )
        
        val response = client.post("/customers") {
            contentType(ContentType.Application.Json)
            setBody(customerCreate)
        }
        
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testLogin() = testApplication {
        configureTestEnvironment()
        application {
            module()
        }
        val client = configureTestClient()
        
        // First register a customer
        val customerCreate = CustomerCreate(
            name = "Jane Doe",
            email = "jane.doe@example.com",
            password = "password123"
        )
        
        client.post("/customers") {
            contentType(ContentType.Application.Json)
            setBody(customerCreate)
        }
        
        // Then login
        val loginRequest = LoginRequest(
            email = "jane.doe@example.com",
            password = "password123"
        )
        
        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(loginRequest)
        }
        
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testLoginWithInvalidCredentials() = testApplication {
        configureTestEnvironment()
        application {
            module()
        }
        val client = configureTestClient()
        
        val loginRequest = LoginRequest(
            email = "nonexistent@example.com",
            password = "wrongpassword"
        )
        
        val response = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(loginRequest)
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testCategoryOperations() = testApplication {
        configureTestEnvironment()
        application {
            module()
        }
        val client = configureTestClient()
        
        // Get authentication token
        val token = getAuthToken(client)
        
        // Create category
        val categoryCreate = CategoryCreate(
            name = "Science Fiction",
            description = "Books about futuristic and scientific themes"
        )
        
        val createResponse = client.post("/categories") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(categoryCreate)
        }
        
        assertEquals(HttpStatusCode.Created, createResponse.status)
        
        // Read all categories (public endpoint)
        val readAllResponse = client.get("/categories")
        assertEquals(HttpStatusCode.OK, readAllResponse.status)
        
        // Read specific category (public endpoint)
        val readOneResponse = client.get("/categories/1")
        assertEquals(HttpStatusCode.OK, readOneResponse.status)
    }

    @Test
    fun testBookOperations() = testApplication {
        configureTestEnvironment()
        application {
            module()
        }
        val client = configureTestClient()
        
        // Get authentication token
        val token = getAuthToken(client)
        
        // First create a category
        val categoryCreate = CategoryCreate(
            name = "Fantasy",
            description = "Fantasy books with magical elements"
        )
        
        client.post("/categories") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(categoryCreate)
        }
        
        // Create book
        val bookCreate = BookCreate(
            title = "The Hobbit",
            author = "J.R.R. Tolkien",
            publisher = "Allen & Unwin",
            publishingYear = 1937,
            categoryId = 1
        )
        
        val createResponse = client.post("/books") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(bookCreate)
        }
        
        assertEquals(HttpStatusCode.Created, createResponse.status)
        
        // Read all books (public endpoint)
        val readAllResponse = client.get("/books")
        assertEquals(HttpStatusCode.OK, readAllResponse.status)
        
        // Read specific book (public endpoint)
        val readOneResponse = client.get("/books/1")
        assertEquals(HttpStatusCode.OK, readOneResponse.status)
    }

    @Test
    fun testUnauthorizedAccess() = testApplication {
        configureTestEnvironment()
        application {
            module()
        }
        val client = configureTestClient()
        
        val categoryCreate = CategoryCreate(
            name = "Test Category",
            description = "Test Description"
        )
        
        // Try to create category without token
        val response = client.post("/categories") {
            contentType(ContentType.Application.Json)
            setBody(categoryCreate)
        }
        
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testCustomerCrudOperations() = testApplication {
        configureTestEnvironment()
        application {
            module()
        }
        val client = configureTestClient()
        
        // Get authentication token
        val token = getAuthToken(client)
        
        // Read all customers
        val readAllResponse = client.get("/customers") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, readAllResponse.status)
        
        // Read specific customer
        val readOneResponse = client.get("/customers/1") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, readOneResponse.status)
        
        // Update customer
        val customerUpdate = CustomerUpdate(
            name = "Updated Name",
            email = null,
            password = null
        )
        
        val updateResponse = client.put("/customers/1") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(customerUpdate)
        }
        assertEquals(HttpStatusCode.OK, updateResponse.status)
    }
    
    private suspend fun getAuthToken(client: io.ktor.client.HttpClient): String {
        // Register a test user
        val customerCreate = CustomerCreate(
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )
        
        client.post("/customers") {
            contentType(ContentType.Application.Json)
            setBody(customerCreate)
        }
        
        // Login to get token
        val loginRequest = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )
        
        val loginResponse = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(loginRequest)
        }
        
        val responseBody = loginResponse.bodyAsText()
        
        // Parse the JSON response to extract the token
        val regex = """"token"\s*:\s*"([^"]+)"""".toRegex()
        val matchResult = regex.find(responseBody)
        return matchResult?.groups?.get(1)?.value ?: throw RuntimeException("Token not found in response: $responseBody")
    }
}
