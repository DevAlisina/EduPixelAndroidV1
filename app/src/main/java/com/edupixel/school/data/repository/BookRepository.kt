package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.EduPixelApiService
import com.edupixel.school.data.remote.models.BookDistribution
import com.edupixel.school.data.remote.models.BookDistributionRegisterResponse
import com.edupixel.school.data.remote.models.DistributionIn

class BookRepository(
    private val api: EduPixelApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun getBookDistributions(classId: Int): NetworkResult<List<BookDistribution>> = safeApiCall {
        api.getBookDistributions(classId)
    }

    suspend fun createBookDistribution(distributionIn: DistributionIn): NetworkResult<BookDistribution> = safeApiCall {
        api.createBookDistribution(distributionIn)
    }

    suspend fun updateBookDistribution(
        distributionId: Int,
        distributionIn: DistributionIn
    ): NetworkResult<BookDistribution> = safeApiCall {
        api.updateBookDistribution(distributionId, distributionIn)
    }

    suspend fun deleteBookDistribution(distributionId: Int): NetworkResult<Unit> = safeApiCall {
        api.deleteBookDistribution(distributionId)
        Unit
    }

    suspend fun getBookDistributionRegister(classId: Int): NetworkResult<BookDistributionRegisterResponse> = safeApiCall {
        api.getBookDistributionRegister(classId)
    }
}
