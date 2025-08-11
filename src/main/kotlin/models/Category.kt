package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Int? = null,
    val name: String,
    val description: String,
    val bookCount: Int = 0
)

@Serializable
data class CategoryCreate(
    val name: String,
    val description: String
)

@Serializable
data class CategoryUpdate(
    val name: String?,
    val description: String?
)
