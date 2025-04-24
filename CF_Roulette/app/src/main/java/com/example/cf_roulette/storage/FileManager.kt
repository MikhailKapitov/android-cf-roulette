package com.example.cf_roulette.storage

import android.content.Context
import com.example.cf_roulette.data.ProblemsetResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileManager(private val context: Context) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(ProblemsetResponse::class.java)

    private val cacheProblemset get() = File(context.filesDir, "problemset_cache.json")

    suspend fun saveProblemset(response: ProblemsetResponse) = withContext(Dispatchers.IO) {
        cacheProblemset.writeText(adapter.toJson(response))
    }

    suspend fun loadProblemset(): ProblemsetResponse? = withContext(Dispatchers.IO) {

        if (!cacheProblemset.exists()) return@withContext null

        try{
            return@withContext adapter.fromJson(cacheProblemset.readText())
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
}