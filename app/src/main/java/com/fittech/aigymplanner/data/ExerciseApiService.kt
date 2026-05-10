package com.fittech.aigymplanner.data

import com.fittech.aigymplanner.model.Exercise
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ExerciseApiService {

    @Headers("X-Api-Key: gRgmEOV7GqyIVOYsKM54L742bBE13hDJ5DHEliYr")   // ← replace with your real key
    @GET("exercises")
    suspend fun getExercises(
        @Query("muscle") muscle: String? = null,
        @Query("type") type: String? = null,
        @Query("difficulty") difficulty: String? = null
    ): Response<List<Exercise>>
}
