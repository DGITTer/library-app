package com.example.services

import com.example.models.*
import com.example.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class BookService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_BOOKS = """
            CREATE TABLE IF NOT EXISTS BOOKS (
                ID SERIAL PRIMARY KEY, 
                TITLE VARCHAR(255) NOT NULL, 
                AUTHOR VARCHAR(255) NOT NULL, 
                PUBLISHER VARCHAR(255) NOT NULL, 
                PUBLISHING_YEAR INT NOT NULL, 
                CATEGORY_ID INT NOT NULL,
                FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORIES(ID)
            );
        """
        private const val SELECT_BOOK_BY_ID = """
            SELECT b.id, b.title, b.author, b.publisher, b.publishing_year, b.category_id, c.name as category_name 
            FROM books b 
            JOIN categories c ON b.category_id = c.id 
            WHERE b.id = ?
        """
        private const val SELECT_ALL_BOOKS = """
            SELECT b.id, b.title, b.author, b.publisher, b.publishing_year, b.category_id, c.name as category_name 
            FROM books b 
            JOIN categories c ON b.category_id = c.id
        """
        private const val INSERT_BOOK = "INSERT INTO books (title, author, publisher, publishing_year, category_id) VALUES (?, ?, ?, ?, ?)"
        private const val UPDATE_BOOK = "UPDATE books SET title = ?, author = ?, publisher = ?, publishing_year = ?, category_id = ? WHERE id = ?"
        private const val DELETE_BOOK = "DELETE FROM books WHERE id = ?"
        private const val CHECK_CATEGORY_EXISTS = "SELECT COUNT(*) FROM categories WHERE id = ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_BOOKS)
    }

    suspend fun create(bookCreate: BookCreate): Book = withContext(Dispatchers.IO) {
        // Validate that category exists
        val checkStatement = connection.prepareStatement(CHECK_CATEGORY_EXISTS)
        checkStatement.setInt(1, bookCreate.categoryId)
        val checkResult = checkStatement.executeQuery()
        if (!checkResult.next() || checkResult.getInt(1) == 0) {
            throw ValidationException("Category does not exist")
        }
        
        val statement = connection.prepareStatement(INSERT_BOOK, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, bookCreate.title)
        statement.setString(2, bookCreate.author)
        statement.setString(3, bookCreate.publisher)
        statement.setInt(4, bookCreate.publishingYear)
        statement.setInt(5, bookCreate.categoryId)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            val id = generatedKeys.getInt(1)
            return@withContext Book(
                id, 
                bookCreate.title, 
                bookCreate.author, 
                bookCreate.publisher, 
                bookCreate.publishingYear, 
                bookCreate.categoryId
            )
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted book")
        }
    }

    suspend fun read(id: Int): Book = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_BOOK_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val bookId = resultSet.getInt("id")
            val title = resultSet.getString("title")
            val author = resultSet.getString("author")
            val publisher = resultSet.getString("publisher")
            val publishingYear = resultSet.getInt("publishing_year")
            val categoryId = resultSet.getInt("category_id")
            val categoryName = resultSet.getString("category_name")
            
            return@withContext Book(bookId, title, author, publisher, publishingYear, categoryId, categoryName)
        } else {
            throw NotFoundException("Book not found")
        }
    }
    
    suspend fun readAll(): List<Book> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_BOOKS)
        val resultSet = statement.executeQuery()
        val books = mutableListOf<Book>()
        
        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val title = resultSet.getString("title")
            val author = resultSet.getString("author")
            val publisher = resultSet.getString("publisher")
            val publishingYear = resultSet.getInt("publishing_year")
            val categoryId = resultSet.getInt("category_id")
            val categoryName = resultSet.getString("category_name")
            
            books.add(Book(id, title, author, publisher, publishingYear, categoryId, categoryName))
        }
        
        return@withContext books
    }

    suspend fun update(id: Int, bookUpdate: BookUpdate): Book = withContext(Dispatchers.IO) {
        // First, get the current book
        val currentBook = read(id)
        
        val title = bookUpdate.title ?: currentBook.title
        val author = bookUpdate.author ?: currentBook.author
        val publisher = bookUpdate.publisher ?: currentBook.publisher
        val publishingYear = bookUpdate.publishingYear ?: currentBook.publishingYear
        val categoryId = bookUpdate.categoryId ?: currentBook.categoryId
        
        // Validate that category exists if it's being updated
        if (bookUpdate.categoryId != null) {
            val checkStatement = connection.prepareStatement(CHECK_CATEGORY_EXISTS)
            checkStatement.setInt(1, categoryId)
            val checkResult = checkStatement.executeQuery()
            if (!checkResult.next() || checkResult.getInt(1) == 0) {
                throw ValidationException("Category does not exist")
            }
        }
        
        val statement = connection.prepareStatement(UPDATE_BOOK)
        statement.setString(1, title)
        statement.setString(2, author)
        statement.setString(3, publisher)
        statement.setInt(4, publishingYear)
        statement.setInt(5, categoryId)
        statement.setInt(6, id)
        val rowsUpdated = statement.executeUpdate()
        
        if (rowsUpdated == 0) {
            throw NotFoundException("Book not found")
        }
        
        return@withContext Book(id, title, author, publisher, publishingYear, categoryId)
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_BOOK)
        statement.setInt(1, id)
        val rowsDeleted = statement.executeUpdate()
        
        if (rowsDeleted == 0) {
            throw NotFoundException("Book not found")
        }
    }
}
