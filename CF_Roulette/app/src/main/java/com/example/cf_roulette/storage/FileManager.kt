package com.example.cf_roulette.storage

import android.content.Context
import com.example.cf_roulette.data.Contest
import com.example.cf_roulette.data.ProblemsetResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileManager(private val context: Context) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapterProblemset = moshi.adapter(ProblemsetResult::class.java)
    private val adapterContestList = moshi.adapter<List<Contest>>(
        Types.newParameterizedType(List::class.java, Contest::class.java)
    )

    private val cacheProblemset get() = File(context.filesDir, "problemset_cache.json")
    private val cacheContestList get() = File(context.filesDir, "contest_list_cache.json")

    suspend fun saveProblemset(result: ProblemsetResult) = withContext(Dispatchers.IO) {
        cacheProblemset.writeText(adapterProblemset.toJson(result))
    }

    suspend fun saveContestList(result: List <Contest>) = withContext(Dispatchers.IO) {
        cacheContestList.writeText(adapterContestList.toJson(result))
    }

    suspend fun loadProblemset(): ProblemsetResult? = withContext(Dispatchers.IO) {

        if (!cacheProblemset.exists()) return@withContext null

        try{
            return@withContext adapterProblemset.fromJson(cacheProblemset.readText())
        }
        catch (e: Exception) {
            return@withContext null
        }
    }

    suspend fun loadContestList(): List <Contest>? = withContext(Dispatchers.IO) {

        if (!cacheContestList.exists()) return@withContext null

        try{
            return@withContext adapterContestList.fromJson(cacheContestList.readText())
        }
        catch (e: Exception) {
            return@withContext null
        }
    }

    suspend fun deleteProblemset() = withContext(Dispatchers.IO) {
        var success = true
        try {
            if (cacheProblemset.exists() && !cacheProblemset.delete()) success = false
        } catch (e: SecurityException) {
            success = false
        }
        return@withContext success
    }

    suspend fun deleteContestList() = withContext(Dispatchers.IO) {
        var success = true
        try {
            if (cacheContestList.exists() && !cacheContestList.delete()) success = false
        } catch (e: SecurityException) {
            success = false
        }
        return@withContext success
    }
}

