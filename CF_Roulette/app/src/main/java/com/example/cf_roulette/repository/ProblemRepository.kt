package com.example.cf_roulette.repository

import android.content.Context
import android.util.Log
import com.example.cf_roulette.data.Problem
import com.example.cf_roulette.data.ProblemsetResult
import com.example.cf_roulette.network.NetworkModule
import com.example.cf_roulette.storage.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

class ProblemRepository private constructor(private val context: Context) {

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
                response.body()?.result?.let {
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

    private suspend fun processProblemset(response: ProblemsetResult?, lowerBound: Int, upperBound: Int, seed: Long?): Problem? {

        val problems = response?.problems ?: return null
        val contestRepository = ContestRepository.getInstance(context)
        val contestList = contestRepository.getContestList()

        val filteredProblems = problems.filter { problem ->
            val passesRating = when {
                lowerBound == 800 && upperBound == 3500 -> true
                problem.rating == null -> false
                else -> problem.rating in lowerBound..upperBound
            }
            if (!passesRating) return@filter false

            val contestId = problem.contestId ?: return@filter false
            val contest = contestRepository.processContestList(contestList, contestId) ?: return@filter false

            val contestMillis = (contest.startTimeSeconds + contest.durationSeconds) * 1000L
            val contestCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = contestMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val nowCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = nowCal.timeInMillis - contestCal.timeInMillis
            val daysSince = diffMillis / (1000 * 60 * 60 * 24)

//            if (daysSince < 128){
//                Log.d("ProblemFiltering", "Too young! " + problem.name + " " + contest.id + " " + daysSince.toString())
//            }

            return@filter daysSince >= 128
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
