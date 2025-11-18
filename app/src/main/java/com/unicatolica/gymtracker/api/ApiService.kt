package com.unicatolica.gymtracker.api

import com.unicatolica.gymtracker.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body user: User): Response<AuthResponse>

    @POST("api/routines")
    suspend fun createRoutine(@Body routine: CreateRoutineRequest): Response<ApiResponse<Routine>>
    @GET("api/routines")
    suspend fun getRoutines(@Query("userId") userId: String): Response<RoutinesResponse>
    @GET("api/profile/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: String
    ): Response<ApiResponse<User>>

    @PUT("profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body user: User
    ): Response<ApiResponse<Any>> // <-- Cambia aquí
}