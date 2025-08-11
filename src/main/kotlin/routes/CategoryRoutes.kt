package com.example.routes

import com.example.models.*
import com.example.services.CategoryService
import com.example.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes(categoryService: CategoryService) {
    
    route("/categories") {
        // Public endpoints - read operations
        get {
            try {
                val categories = categoryService.readAll()
                call.respond(HttpStatusCode.OK, categories)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
            }
        }
        
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("Invalid ID")
                val category = categoryService.read(id)
                call.respond(HttpStatusCode.OK, category)
            } catch (e: ValidationException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Invalid ID"))
            } catch (e: NotFoundException) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", e.message ?: "Category not found"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
            }
        }
        
        // Protected endpoints - write operations
        authenticate {
            post {
                try {
                    val categoryCreate = call.receive<CategoryCreate>()
                    val category = categoryService.create(categoryCreate)
                    call.respond(HttpStatusCode.Created, category)
                } catch (e: ValidationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Validation failed"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
                }
            }
            
            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("Invalid ID")
                    val categoryUpdate = call.receive<CategoryUpdate>()
                    val category = categoryService.update(id, categoryUpdate)
                    call.respond(HttpStatusCode.OK, category)
                } catch (e: ValidationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Validation failed"))
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", e.message ?: "Category not found"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
                }
            }
            
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("Invalid ID")
                    categoryService.delete(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: ValidationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Invalid ID"))
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", e.message ?: "Category not found"))
                } catch (e: ConflictException) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse("conflict", e.message ?: "Cannot delete category with associated books"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
                }
            }
        }
    }
}
