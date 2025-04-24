package com.example.cf_roulette.repository

import android.util.Log
import com.example.cf_roulette.api.CodeforcesApiService
import com.example.cf_roulette.data.Problem
import com.example.cf_roulette.network.NetworkModule
import kotlin.random.Random

object ProblemRepository {
    private val apiService: CodeforcesApiService = NetworkModule.codeforcesApi

    suspend fun getProblem(lowerBound: Int, upperBound: Int, randomSeed: Long? = null): Problem? {

        // Currently always using the network.

        try {
            val response = apiService.getProblemset()
            if (response.isSuccessful && response.body()?.status == "OK") {
                val problems = response.body()?.result?.problems ?: emptyList()
                val potentialProblems = problems.filter { problem ->
                    when {
                        lowerBound == 800 && upperBound == 3500 -> true
                        problem.rating == null -> false
                        else -> problem.rating in lowerBound..upperBound
                    }
                }
                return getRandomProblem(potentialProblems, randomSeed)
            }
            return null
        }
        catch (e: Exception) {
            Log.e("ProblemRepository", "Network error: ${e.message}")
            return null
        }
    }

    private fun getRandomProblem(problems: List<Problem>, seed: Long?): Problem? {

        if (problems.isEmpty()) return null

        if (seed != null) {
            val seededRandom = Random(seed)
            return problems[seededRandom.nextInt(problems.size)]
        }

        return problems.randomOrNull()

    }
}
