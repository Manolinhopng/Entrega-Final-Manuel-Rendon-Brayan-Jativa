package com.unicatolica.gymtracker.api

import com.unicatolica.gymtracker.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body user: User): Response<AuthResponse>

    @POST("routines")
    suspend fun createRoutine(@Body routine: Routine): Response<Routine>

    @GET("routines")
    suspend fun getRoutines(@Query("userId") userId: String): Response<List<Routine>>

    @POST("routines")
    suspend fun createRoutine(@Body routine: Routine): Response<Routine>

    @GET("routines")
    suspend fun getRoutines(@Query("userId") userId: String): Response<List<Routine>>

    @POST("register")
    suspend fun register(@Body user: User): Response<AuthResponse>

    @GET("profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<User>

    @PUT("profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body user: User
    ): Response<User>

}