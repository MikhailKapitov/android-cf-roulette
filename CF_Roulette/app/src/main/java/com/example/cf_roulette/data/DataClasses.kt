package com.example.cf_roulette.data

import com.squareup.moshi.Json

data class ProblemsetResponse(
    @Json(name = "status") val status: String,
    @Json(name = "result") val result: ProblemsetResult
)

data class ProblemsetResult(
    @Json(name = "problems") val problems: List<Problem>
    // Also "problemStatistics", probably not needed though.
)

data class Problem(
    @Json(name = "contestId") val contestId: Int?,
    @Json(name = "index") val index: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "rating") val rating: Int?,
    @Json(name = "tags") val tags: List <String>?
    // Also "type", probably not needed.
)

data class ContestListResponse(
    @Json(name = "status") val status: String,
    @Json(name = "result") val result: List <Contest>
)

data class Contest(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String, // Only useful for testing.
    @Json(name = "type") val type: String,
    @Json(name = "durationSeconds") val durationSeconds: Int,
    @Json(name = "startTimeSeconds") val startTimeSeconds: Int
    // Also "phase", "frozen", "relativeTimeSeconds".
)

data class UserStatusResponse(
    @Json(name = "status") val status: String,
    @Json(name = "result") val result: List <Submission>
)

data class Submission(
    @Json(name = "problem") val problem: Problem,
    @Json(name = "creationTimeSeconds") val creationTimeSeconds: Int,
    @Json(name = "verdict") val verdict: String
    // Also "id", "contestId", "relativeTimeSeconds", "author", "programmingLanguage", "testset", "passedTestCount", "timeConsumedMillis", "memoryConsumedBytes", "points".
)
