package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: Int? = null,
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class CustomerCreate(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class CustomerUpdate(
    val name: String?,
    val email: String?,
    val password: String?
)

@Serializable
data class CustomerResponse(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val customer: CustomerResponse
)
