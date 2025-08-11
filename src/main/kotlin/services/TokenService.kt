package com.example.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.CustomerResponse
import java.util.*

class TokenService {
    companion object {
        private const val JWT_SECRET = "secret"
        private const val JWT_ISSUER = "http://localhost:8080/"
        private const val JWT_AUDIENCE = "jwt-audience"
        private const val JWT_REALM = "library-app"
        private const val EXPIRATION_TIME = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    fun generateToken(customer: CustomerResponse): String {
        return JWT.create()
            .withAudience(JWT_AUDIENCE)
            .withIssuer(JWT_ISSUER)
            .withClaim("customerId", customer.id)
            .withClaim("email", customer.email)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .sign(Algorithm.HMAC256(JWT_SECRET))
    }
}
