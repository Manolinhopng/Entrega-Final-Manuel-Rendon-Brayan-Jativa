package com.unicatolica.gymtracker.api

import com.unicatolica.gymtracker.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---------------- AUTH ----------------
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body user: User): Response<AuthResponse>


    // ---------------- ROUTINES ----------------
    @POST("api/routines")
    suspend fun createRoutine(@Body routine: CreateRoutineRequest): Response<ApiResponse<Routine>>

    @GET("api/routines")
    suspend fun getRoutines(@Query("userId") userId: String): Response<RoutinesResponse>

    @GET("api/routines/stats")
    suspend fun getTrainingStats(@Query("userId") userId: String): Response<StatsResponse>

    // ✅ CORREGIDO: La variable de ruta es 'id', no 'userId'
    @PATCH("api/routines/{id}/finish")
    suspend fun finishRoutine(
        @Path("id") routineId: String, // El nombre de la variable en Kotlin puede ser 'routineId' si es más claro
        @Body body: Map<String, String>
    ): Response<ApiResponse<Any>>


    // ---------------- GET ROUTINE BY ID ----------------
    // ✅ CORREGIDO: La variable de ruta es 'id', y se añade 'userId' como parámetro de consulta
    @GET("api/routines/{id}")
    suspend fun getRoutineById(
        @Path("id") id: String,
        @Query("userId") userId: String
    ): Response<RoutineResponse>

    // ---------------- UPDATE ROUTINE ----------------
    // ✅ CORREGIDO: La variable de ruta es 'id', no 'userId'
    @PUT("api/routines/{id}") // <-- Cambiado de {userId} a {id}
    suspend fun updateRoutine(
        @Path("id") id: String, // <-- Cambiado de @Path("userId") a @Path("id")
        @Body body: Map<String, String>
    ): Response<ApiResponse<Any>>


    // ---------------- PROFILE ----------------
    @GET("api/profile/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: String
    ): Response<ApiResponse<User>>

    @PUT("api/profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body user: User
    ): Response<ApiResponse<Any>>


    // ---------------- WEIGHT HISTORY ----------------
    @GET("api/users/{userId}/weight-history")
    suspend fun getWeightHistory(@Path("userId") userId: String): List<WeightEntry>
}