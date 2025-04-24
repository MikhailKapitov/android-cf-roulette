package com.example.cf_roulette.api

import com.example.cf_roulette.data.ProblemsetResponse
import retrofit2.Response
import retrofit2.http.GET

interface CodeforcesApiService {
    @GET("problemset.problems")
    suspend fun getProblemset(): Response<ProblemsetResponse>
}
