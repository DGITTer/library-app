package com.example.routes

import com.example.models.*
import com.example.services.CustomerService
import com.example.services.TokenService
import com.example.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.customerRoutes(customerService: CustomerService, tokenService: TokenService) {
    
    // Public endpoints
    route("/customers") {
        // Register new customer
        post {
            try {
                val customerCreate = call.receive<CustomerCreate>()
                val customer = customerService.create(customerCreate)
                call.respond(HttpStatusCode.Created, customer)
            } catch (e: ValidationException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Validation failed"))
            } catch (e: ConflictException) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("conflict", e.message ?: "Resource conflict"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
            }
        }
    }
    
    // Login endpoint
    post("/login") {
        try {
            val loginRequest = call.receive<LoginRequest>()
            val customer = customerService.authenticate(loginRequest.email, loginRequest.password)
            val token = tokenService.generateToken(customer)
            call.respond(HttpStatusCode.OK, LoginResponse(token, customer))
        } catch (e: UnauthorizedException) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("unauthorized", e.message ?: "Invalid credentials"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
        }
    }
    
    // Protected endpoints
    authenticate {
        route("/customers") {
            // Get all customers
            get {
                try {
                    val customers = customerService.readAll()
                    call.respond(HttpStatusCode.OK, customers)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
                }
            }
            
            // Get customer by ID
            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("Invalid ID")
                    val customer = customerService.read(id)
                    call.respond(HttpStatusCode.OK, customer)
                } catch (e: ValidationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Invalid ID"))
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", e.message ?: "Customer not found"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
                }
            }
            
            // Update customer
            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("Invalid ID")
                    val customerUpdate = call.receive<CustomerUpdate>()
                    val customer = customerService.update(id, customerUpdate)
                    call.respond(HttpStatusCode.OK, customer)
                } catch (e: ValidationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Validation failed"))
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", e.message ?: "Customer not found"))
                } catch (e: ConflictException) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse("conflict", e.message ?: "Resource conflict"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
                }
            }
            
            // Delete customer
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("Invalid ID")
                    customerService.delete(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: ValidationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Invalid ID"))
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", e.message ?: "Customer not found"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
                }
            }
        }
    }
}
