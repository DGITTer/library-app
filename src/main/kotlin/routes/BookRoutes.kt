package com.example.routes

import com.example.models.*
import com.example.services.BookService
import com.example.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bookRoutes(bookService: BookService) {
    
    route("/books") {
        // Public endpoints - read operations
        get {
            try {
                val books = bookService.readAll()
                call.respond(HttpStatusCode.OK, books)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
            }
        }
        
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("Invalid ID")
                val book = bookService.read(id)
                call.respond(HttpStatusCode.OK, book)
            } catch (e: ValidationException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Invalid ID"))
            } catch (e: NotFoundException) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", e.message ?: "Book not found"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
            }
        }
        
        // Protected endpoints - write operations
        authenticate {
            post {
                try {
                    val bookCreate = call.receive<BookCreate>()
                    val book = bookService.create(bookCreate)
                    call.respond(HttpStatusCode.Created, book)
                } catch (e: ValidationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Validation failed"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
                }
            }
            
            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("Invalid ID")
                    val bookUpdate = call.receive<BookUpdate>()
                    val book = bookService.update(id, bookUpdate)
                    call.respond(HttpStatusCode.OK, book)
                } catch (e: ValidationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Validation failed"))
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", e.message ?: "Book not found"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
                }
            }
            
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("Invalid ID")
                    bookService.delete(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: ValidationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("validation_error", e.message ?: "Invalid ID"))
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", e.message ?: "Book not found"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error", "Internal server error"))
                }
            }
        }
    }
}
