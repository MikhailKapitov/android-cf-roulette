package com.example.cf_roulette.repository

import android.content.Context
import android.util.Log
import com.example.cf_roulette.data.Problem
import com.example.cf_roulette.data.ProblemsetResponse
import com.example.cf_roulette.network.NetworkModule
import com.example.cf_roulette.storage.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ProblemRepository private constructor(context: Context) {

    private val apiService = NetworkModule.codeforcesApi
    private val fileManager = FileManager(context)

    companion object {
        private var INSTANCE: ProblemRepository? = null

        fun getInstance(context: Context): ProblemRepository {
            if (INSTANCE == null) {
                INSTANCE = ProblemRepository(context)
            }
            return INSTANCE!!
        }
    }

    suspend fun getProblem(lowerBound: Int, upperBound: Int, randomSeed: Long? = null): Problem? {
        try {
            val cachedResponse = fileManager.loadProblemset()
            return processProblemset(cachedResponse, lowerBound, upperBound, randomSeed)
        } catch (e: Exception) {
            Log.e("ProblemRepository", "Cache read error: ${e.message}")
            return null
        }
    }

    suspend fun updateCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProblemset()
            if (response.isSuccessful && response.body()?.status == "OK") {
                response.body()?.let {
                    fileManager.saveProblemset(it)
                    return@withContext true
                }
            }
            false
        } catch (e: Exception) {
            Log.e("ProblemRepository", "Cache update failed: ${e.message}")
            false
        }
    }

    suspend fun deleteCache() : Boolean = withContext(Dispatchers.IO) {
        fileManager.deleteProblemset()
    }

    private fun processProblemset(response: ProblemsetResponse?, lowerBound: Int, upperBound: Int, seed: Long?): Problem? {
        val problems = response?.result?.problems ?: return null
        val filteredProblems = problems.filter { problem ->
            when {
                lowerBound == 800 && upperBound == 3500 -> true
                problem.rating == null -> false
                else -> problem.rating in lowerBound..upperBound
            }
        }
        return getRandomProblem(filteredProblems, seed)
    }

    private fun getRandomProblem(problems: List<Problem>, seed: Long?): Problem? {
        if (problems.isEmpty()){
            return null
        }
        if (seed != null) {
            val random = Random(seed)
            return problems.random(random)
        }
        return problems.randomOrNull()
    }
}
