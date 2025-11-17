package com.unicatolica.gymtracker.data

data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val success: Boolean, val userId: String?, val message: String?)