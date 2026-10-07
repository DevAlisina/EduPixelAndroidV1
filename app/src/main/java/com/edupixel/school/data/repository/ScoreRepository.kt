package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.EduPixelApiService
import com.edupixel.school.data.remote.models.Score
import com.edupixel.school.data.remote.models.ScoreBulkIn
import com.edupixel.school.data.remote.models.ScoreIn
import com.edupixel.school.data.remote.models.ShaqahResponse
import kotlinx.serialization.json.JsonObject

class ScoreRepository(
    private val api: EduPixelApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun upsertScore(scoreIn: ScoreIn): NetworkResult<Score> = safeApiCall {
        api.upsertScore(scoreIn)
    }

    suspend fun bulkUpsertScores(classId: Int, items: List<ScoreIn>): NetworkResult<List<Score>> = safeApiCall {
        api.bulkUpsertScores(classId, ScoreBulkIn(items))
    }

    suspend fun getStudentScores(studentId: Int): NetworkResult<List<Score>> = safeApiCall {
        api.getStudentScores(studentId)
    }

    suspend fun getScore(scoreId: Int): NetworkResult<Score> = safeApiCall {
        api.getScore(scoreId)
    }

    suspend fun updateScore(scoreId: Int, scoreIn: ScoreIn): NetworkResult<Score> = safeApiCall {
        api.updateScore(scoreId, scoreIn)
    }

    suspend fun deleteScore(scoreId: Int): NetworkResult<Unit> = safeApiCall {
        api.deleteScore(scoreId)
        Unit
    }

    suspend fun getScoreComponents(scoreId: Int): NetworkResult<JsonObject> = safeApiCall {
        api.getScoreComponents(scoreId)
    }

    suspend fun getStudentShaqah(studentId: Int): NetworkResult<ShaqahResponse> = safeApiCall {
        api.getStudentShaqah(studentId)
    }
}
