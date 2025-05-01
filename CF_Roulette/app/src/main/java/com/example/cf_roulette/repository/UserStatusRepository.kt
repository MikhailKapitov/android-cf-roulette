package com.example.cf_roulette.repository

import android.content.Context
import android.util.Log
import com.example.cf_roulette.data.Problem
import com.example.cf_roulette.network.NetworkModule
import com.example.cf_roulette.storage.FileManager

class UserStatusRepository private constructor(context: Context) {

    private val apiService = NetworkModule.codeforcesApi
    private val fileManager = FileManager(context)

    companion object {
        private var INSTANCE: UserStatusRepository? = null

        fun getInstance(context: Context): UserStatusRepository {
            if (INSTANCE == null) {
                INSTANCE = UserStatusRepository(context)
            }
            return INSTANCE!!
        }
    }

    private fun getVerdictNumber(verditcs: List <String>): Int{

        if (verditcs.isEmpty()){
            return 0
        }

        if ("OK" in verditcs){
            return  1
        }

        if ("TESTING" in verditcs || "SUBMITTED" in verditcs){
            return 3
        }

        return 2
    }

    suspend fun checkStatus(handle: String, problems: List<Problem>): List<Int> {

        val results = mutableListOf<Int>()

        try {
            val response = apiService.getUserStatus(handle, 1, 128)
            if (response.isSuccessful && response.body()?.status == "OK") {
                val submissions = response.body()?.result ?: emptyList()

                for (problem in problems) {
                    val matchingSubmissions = submissions.filter { submission ->
                        val subProblem = submission.problem
                        subProblem.contestId == problem.contestId &&
                                subProblem.index == problem.index
                    }

                    val verdicts = matchingSubmissions.mapNotNull { it.verdict }
                    val evaluation = getVerdictNumber(verdicts)
                    results.add(evaluation)
                }
            } else {
                return List(problems.size) { 3 }
            }
        } catch (e: Exception) {
            Log.e("UserStatusRepository", "Status retrieval failed: ${e.message}")
            return List(problems.size) { 3 }
        }

        return results
    }

}
