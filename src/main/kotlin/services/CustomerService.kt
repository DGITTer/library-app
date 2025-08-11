package com.example.services

import com.example.models.*
import com.example.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.Statement

class CustomerService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_CUSTOMERS = """
            CREATE TABLE IF NOT EXISTS CUSTOMERS (
                ID SERIAL PRIMARY KEY, 
                NAME VARCHAR(255) NOT NULL, 
                EMAIL VARCHAR(255) UNIQUE NOT NULL, 
                PASSWORD VARCHAR(255) NOT NULL
            );
        """
        private const val SELECT_CUSTOMER_BY_ID = "SELECT id, name, email, password FROM customers WHERE id = ?"
        private const val SELECT_CUSTOMER_BY_EMAIL = "SELECT id, name, email, password FROM customers WHERE email = ?"
        private const val SELECT_ALL_CUSTOMERS = "SELECT id, name, email, password FROM customers"
        private const val INSERT_CUSTOMER = "INSERT INTO customers (name, email, password) VALUES (?, ?, ?)"
        private const val UPDATE_CUSTOMER = "UPDATE customers SET name = ?, email = ?, password = ? WHERE id = ?"
        private const val DELETE_CUSTOMER = "DELETE FROM customers WHERE id = ?"
        private const val CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM customers WHERE email = ? AND id != ?"
    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_CUSTOMERS)
    }

    suspend fun create(customerCreate: CustomerCreate): CustomerResponse = withContext(Dispatchers.IO) {
        // Validate email
        if (!EmailValidator.isValid(customerCreate.email)) {
            throw ValidationException("Invalid email format")
        }
        
        // Check if email already exists
        val checkStatement = connection.prepareStatement("SELECT COUNT(*) FROM customers WHERE email = ?")
        checkStatement.setString(1, customerCreate.email)
        val checkResult = checkStatement.executeQuery()
        if (checkResult.next() && checkResult.getInt(1) > 0) {
            throw ConflictException("Email already exists")
        }
        
        val hashedPassword = PasswordUtils.hashPassword(customerCreate.password)
        val statement = connection.prepareStatement(INSERT_CUSTOMER, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, customerCreate.name)
        statement.setString(2, customerCreate.email)
        statement.setString(3, hashedPassword)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            val id = generatedKeys.getInt(1)
            return@withContext CustomerResponse(id, customerCreate.name, customerCreate.email)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted customer")
        }
    }

    suspend fun read(id: Int): CustomerResponse = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_CUSTOMER_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val customerId = resultSet.getInt("id")
            val name = resultSet.getString("name")
            val email = resultSet.getString("email")
            return@withContext CustomerResponse(customerId, name, email)
        } else {
            throw NotFoundException("Customer not found")
        }
    }
    
    suspend fun readAll(): List<CustomerResponse> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_CUSTOMERS)
        val resultSet = statement.executeQuery()
        val customers = mutableListOf<CustomerResponse>()
        
        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val name = resultSet.getString("name")
            val email = resultSet.getString("email")
            customers.add(CustomerResponse(id, name, email))
        }
        
        return@withContext customers
    }

    suspend fun update(id: Int, customerUpdate: CustomerUpdate): CustomerResponse = withContext(Dispatchers.IO) {
        // First, get the current customer
        val currentCustomer = readInternal(id)
        
        val name = customerUpdate.name ?: currentCustomer.name
        val email = customerUpdate.email ?: currentCustomer.email
        val password = customerUpdate.password?.let { PasswordUtils.hashPassword(it) } ?: currentCustomer.password
        
        // Validate email if it's being updated
        if (customerUpdate.email != null && !EmailValidator.isValid(email)) {
            throw ValidationException("Invalid email format")
        }
        
        // Check if email already exists for another customer
        if (customerUpdate.email != null) {
            val checkStatement = connection.prepareStatement(CHECK_EMAIL_EXISTS)
            checkStatement.setString(1, email)
            checkStatement.setInt(2, id)
            val checkResult = checkStatement.executeQuery()
            if (checkResult.next() && checkResult.getInt(1) > 0) {
                throw ConflictException("Email already exists")
            }
        }
        
        val statement = connection.prepareStatement(UPDATE_CUSTOMER)
        statement.setString(1, name)
        statement.setString(2, email)
        statement.setString(3, password)
        statement.setInt(4, id)
        val rowsUpdated = statement.executeUpdate()
        
        if (rowsUpdated == 0) {
            throw NotFoundException("Customer not found")
        }
        
        return@withContext CustomerResponse(id, name, email)
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_CUSTOMER)
        statement.setInt(1, id)
        val rowsDeleted = statement.executeUpdate()
        
        if (rowsDeleted == 0) {
            throw NotFoundException("Customer not found")
        }
    }
    
    suspend fun authenticate(email: String, password: String): CustomerResponse = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_CUSTOMER_BY_EMAIL)
        statement.setString(1, email)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val id = resultSet.getInt("id")
            val name = resultSet.getString("name")
            val storedPassword = resultSet.getString("password")
            
            if (PasswordUtils.verifyPassword(password, storedPassword)) {
                return@withContext CustomerResponse(id, name, email)
            } else {
                throw UnauthorizedException("Invalid credentials")
            }
        } else {
            throw UnauthorizedException("Invalid credentials")
        }
    }
    
    private suspend fun readInternal(id: Int): Customer = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_CUSTOMER_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val customerId = resultSet.getInt("id")
            val name = resultSet.getString("name")
            val email = resultSet.getString("email")
            val password = resultSet.getString("password")
            return@withContext Customer(customerId, name, email, password)
        } else {
            throw NotFoundException("Customer not found")
        }
    }
}
