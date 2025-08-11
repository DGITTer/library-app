package com.example.utils

import java.util.regex.Pattern

object EmailValidator {
    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    )
    
    fun isValid(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }
}

object PasswordUtils {
    fun hashPassword(password: String): String {
        // In a real application, use a proper password hashing library like BCrypt
        // For this example, we'll use a simple hash
        return password.hashCode().toString()
    }
    
    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return hashPassword(password) == hashedPassword
    }
}
