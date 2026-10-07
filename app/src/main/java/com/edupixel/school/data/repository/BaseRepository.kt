package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkErrorParser
import com.edupixel.school.core.network.NetworkResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
        return withContext(dispatcher) {
            try {
                NetworkResult.Success(apiCall())
            } catch (e: Throwable) {
                NetworkErrorParser.parseException(e)
            }
        }
    }
}
