package com.example.cf_roulette.api

import com.example.cf_roulette.data.ContestListResponse
import com.example.cf_roulette.data.ProblemsetResponse
import com.example.cf_roulette.data.UserStatusResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CodeforcesApiService {
    @GET("problemset.problems")
    suspend fun getProblemset(): Response<ProblemsetResponse>
    @GET("contest.list")
    suspend fun getContestList(): Response<ContestListResponse>
    @GET("user.status")
    suspend fun getUserStatus(
        @Query("handle") handle: String,
        @Query("from") from: Int,
        @Query("count") count: Int
    ): Response<UserStatusResponse>
}
