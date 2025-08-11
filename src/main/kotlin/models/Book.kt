package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: Int? = null,
    val title: String,
    val author: String,
    val publisher: String,
    val publishingYear: Int,
    val categoryId: Int,
    val categoryName: String? = null
)

@Serializable
data class BookCreate(
    val title: String,
    val author: String,
    val publisher: String,
    val publishingYear: Int,
    val categoryId: Int
)

@Serializable
data class BookUpdate(
    val title: String?,
    val author: String?,
    val publisher: String?,
    val publishingYear: Int?,
    val categoryId: Int?
)
