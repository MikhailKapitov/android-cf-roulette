package com.example.cf_roulette.repository

import android.content.Context
import android.util.Log
import com.example.cf_roulette.data.Problem
import com.example.cf_roulette.data.ProblemsetResult
import com.example.cf_roulette.network.NetworkModule
import com.example.cf_roulette.storage.FileManager
import com.example.cf_roulette.util.HashingUtil.sha256
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

class ProblemRepository private constructor(private val context: Context) {

    private val apiService = NetworkModule.codeforcesApi
    private val fileManager = FileManager(context)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter<List<Problem>>(
        Types.newParameterizedType(List::class.java, Problem::class.java)
    )

    companion object {
        private var INSTANCE: ProblemRepository? = null

        fun getInstance(context: Context): ProblemRepository {
            if (INSTANCE == null) {
                INSTANCE = ProblemRepository(context)
            }
            return INSTANCE!!
        }
    }

    suspend fun getProblemset(lowerBound: Int, upperBound: Int, dayCount: Int, goodTags: List <String>, tagOring: Boolean, specialTagBanned: Boolean): List <Problem>? {
        try {
            val cachedResponse = fileManager.loadProblemset()
            val filteredProblems = processProblemset(cachedResponse, lowerBound, upperBound, dayCount, goodTags, tagOring, specialTagBanned)
            return filteredProblems
        } catch (e: Exception) {
            Log.e("ProblemRepository", "Cache read error: ${e.message}")
            return null
        }
    }

    suspend fun getProblem(lowerBound: Int, upperBound: Int, dayCount: Int, goodTags: List <String>, tagOring: Boolean, specialTagBanned: Boolean, randomSeed: Long? = null): Problem? {
        val filteredProblems = getProblemset(lowerBound, upperBound, dayCount, goodTags, tagOring, specialTagBanned)
        if (filteredProblems == null){
            return  null
        }
        return getRandomProblem(filteredProblems, randomSeed)
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

    private suspend fun processProblemset(response: ProblemsetResult?, lowerBound: Int, upperBound: Int, dayCount: Int, goodTags: List <String>, tagOring: Boolean, specialTagBanned: Boolean): List <Problem>? {

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

            if (daysSince < dayCount){
                Log.d("ProblemFiltering", "Too young! " + problem.name + " " + contest.id + " " + daysSince.toString())
                return@filter false
            }

            val tags = problem.tags

            if(tags == null){
                if (specialTagBanned || goodTags.isNotEmpty()){
                    return@filter false
                }
                return@filter true
            }

            if (specialTagBanned && "*special" in tags) {
                return@filter false
            }

            if(tagOring){
                for (tag in goodTags) {
                    if (tag in tags){
                        return@filter true
                    }
                }
                return@filter false
            }

            for (tag in goodTags) {
                if (tag !in tags){
                    return@filter false
                }
            }
            return@filter true
        }

        return filteredProblems
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

    fun hashProblemList(problems: List<Problem>): String {
        val sorted = problems.sortedWith(compareBy({ it.contestId }, { it.index }))
        val json = adapter.toJson(sorted)
        return sha256(json)
    }
}
