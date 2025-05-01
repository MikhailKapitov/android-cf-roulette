package com.example.cf_roulette.repository

import android.content.Context
import android.util.Log
import com.example.cf_roulette.data.Contest
import com.example.cf_roulette.network.NetworkModule
import com.example.cf_roulette.storage.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContestRepository private constructor(context: Context) {

    private val apiService = NetworkModule.codeforcesApi
    private val fileManager = FileManager(context)

    companion object {
        private var INSTANCE: ContestRepository? = null

        fun getInstance(context: Context): ContestRepository {
            if (INSTANCE == null) {
                INSTANCE = ContestRepository(context)
            }
            return INSTANCE!!
        }
    }

    suspend fun getContest(contestId: Int): Contest? {
        try {
            val cachedResponse = fileManager.loadContestList()
            return processContestList(cachedResponse, contestId)
        } catch (e: Exception) {
            Log.e("ContestRepository", "Cache read error: ${e.message}")
            return null
        }
    }

    suspend fun updateCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getContestList()
            if (response.isSuccessful && response.body()?.status == "OK") {
                response.body()?.result?.let {
                    fileManager.saveContestList(it)
                    return@withContext true
                }
            }
            false
        } catch (e: Exception) {
            Log.e("ContestRepository", "Cache update failed: ${e.message}")
            false
        }
    }

    suspend fun deleteCache(): Boolean = withContext(Dispatchers.IO) {
        fileManager.deleteContestList()
    }

    private fun processContestList(response: List<Contest>?, contestId: Int): Contest? {
        val contests = response ?: return null
        val contest = contests.firstOrNull { it.id == contestId }
        return contest
    }

}
