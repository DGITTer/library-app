package com.example.services

import com.example.models.*
import com.example.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class CategoryService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_CATEGORIES = """
            CREATE TABLE IF NOT EXISTS CATEGORIES (
                ID SERIAL PRIMARY KEY, 
                NAME VARCHAR(255) NOT NULL, 
                DESCRIPTION TEXT NOT NULL
            );
        """
        private const val SELECT_CATEGORY_BY_ID = """
            SELECT c.id, c.name, c.description, COUNT(b.id) as book_count 
            FROM categories c 
            LEFT JOIN books b ON c.id = b.category_id 
            WHERE c.id = ? 
            GROUP BY c.id, c.name, c.description
        """
        private const val SELECT_ALL_CATEGORIES = """
            SELECT c.id, c.name, c.description, COUNT(b.id) as book_count 
            FROM categories c 
            LEFT JOIN books b ON c.id = b.category_id 
            GROUP BY c.id, c.name, c.description
        """
        private const val INSERT_CATEGORY = "INSERT INTO categories (name, description) VALUES (?, ?)"
        private const val UPDATE_CATEGORY = "UPDATE categories SET name = ?, description = ? WHERE id = ?"
        private const val DELETE_CATEGORY = "DELETE FROM categories WHERE id = ?"
        private const val CHECK_CATEGORY_HAS_BOOKS = "SELECT COUNT(*) FROM books WHERE category_id = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_CATEGORIES)
    }

    suspend fun create(categoryCreate: CategoryCreate): Category = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_CATEGORY, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, categoryCreate.name)
        statement.setString(2, categoryCreate.description)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            val id = generatedKeys.getInt(1)
            return@withContext Category(id, categoryCreate.name, categoryCreate.description, 0)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted category")
        }
    }

    suspend fun read(id: Int): Category = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_CATEGORY_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val categoryId = resultSet.getInt("id")
            val name = resultSet.getString("name")
            val description = resultSet.getString("description")
            val bookCount = resultSet.getInt("book_count")
            return@withContext Category(categoryId, name, description, bookCount)
        } else {
            throw NotFoundException("Category not found")
        }
    }
    
    suspend fun readAll(): List<Category> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_CATEGORIES)
        val resultSet = statement.executeQuery()
        val categories = mutableListOf<Category>()
        
        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val name = resultSet.getString("name")
            val description = resultSet.getString("description")
            val bookCount = resultSet.getInt("book_count")
            categories.add(Category(id, name, description, bookCount))
        }
        
        return@withContext categories
    }

    suspend fun update(id: Int, categoryUpdate: CategoryUpdate): Category = withContext(Dispatchers.IO) {
        // First, get the current category
        val currentCategory = read(id)
        
        val name = categoryUpdate.name ?: currentCategory.name
        val description = categoryUpdate.description ?: currentCategory.description
        
        val statement = connection.prepareStatement(UPDATE_CATEGORY)
        statement.setString(1, name)
        statement.setString(2, description)
        statement.setInt(3, id)
        val rowsUpdated = statement.executeUpdate()
        
        if (rowsUpdated == 0) {
            throw NotFoundException("Category not found")
        }
        
        return@withContext Category(id, name, description, currentCategory.bookCount)
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        // Check if category has books
        val checkStatement = connection.prepareStatement(CHECK_CATEGORY_HAS_BOOKS)
        checkStatement.setInt(1, id)
        val checkResult = checkStatement.executeQuery()
        if (checkResult.next() && checkResult.getInt(1) > 0) {
            throw ConflictException("Cannot delete category with associated books")
        }
        
        val statement = connection.prepareStatement(DELETE_CATEGORY)
        statement.setInt(1, id)
        val rowsDeleted = statement.executeUpdate()
        
        if (rowsDeleted == 0) {
            throw NotFoundException("Category not found")
        }
    }
}
