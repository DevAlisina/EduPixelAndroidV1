package com.edupixel.school.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(
        val code: Int? = null,
        val message: String,
        val details: List<String> = emptyList(),
        val isNetworkError: Boolean = false
    ) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = if (this is Success) data else null
}

object NetworkErrorParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parseException(throwable: Throwable): NetworkResult.Error {
        return when (throwable) {
            is ConnectException -> {
                NetworkResult.Error(
                    code = null,
                    message = "Cannot connect to EduPixel API. Check that the backend is running on port 7878.",
                    isNetworkError = true
                )
            }
            is SocketTimeoutException -> {
                NetworkResult.Error(
                    code = null,
                    message = "Connection timed out while communicating with the EduPixel server.",
                    isNetworkError = true
                )
            }
            is IOException -> {
                NetworkResult.Error(
                    code = null,
                    message = "Network error: Unable to reach the server. Please verify your connection.",
                    isNetworkError = true
                )
            }
            is HttpException -> {
                val code = throwable.code()
                val errorBody = throwable.response()?.errorBody()?.string()
                parseHttpError(code, errorBody)
            }
            else -> {
                NetworkResult.Error(
                    code = null,
                    message = throwable.localizedMessage ?: "An unexpected error occurred."
                )
            }
        }
    }

    private fun parseHttpError(code: Int, errorBody: String?): NetworkResult.Error {
        if (errorBody.isNullOrBlank()) {
            val message = when (code) {
                400 -> "Bad request. Please review the submitted data."
                404 -> "Requested item was not found."
                409 -> "A conflict occurred. Record already exists or violates constraints."
                422 -> "Validation failed for the submitted input."
                500 -> "Internal server error. Please verify the backend logs."
                else -> "Server error ($code)."
            }
            return NetworkResult.Error(code = code, message = message)
        }

        try {
            val element = json.parseToJsonElement(errorBody)
            if (element is JsonObject && element.containsKey("detail")) {
                val detail = element["detail"]
                when (detail) {
                    is JsonArray -> {
                        val messages = detail.mapNotNull { item ->
                            if (item is JsonObject) {
                                val msg = item["msg"]?.jsonPrimitive?.content
                                val loc = (item["loc"] as? JsonArray)?.mapNotNull { it.jsonPrimitive.content }?.joinToString(".")
                                if (loc != null && msg != null) "$loc: $msg" else msg
                            } else {
                                item.jsonPrimitive.content
                            }
                        }
                        return NetworkResult.Error(
                            code = code,
                            message = if (messages.isNotEmpty()) messages.first() else "Validation error ($code)",
                            details = messages
                        )
                    }
                    is JsonObject -> {
                        return NetworkResult.Error(
                            code = code,
                            message = detail.toString()
                        )
                    }
                    else -> {
                        val msg = detail?.jsonPrimitive?.content ?: "Error ($code)"
                        return NetworkResult.Error(code = code, message = msg)
                    }
                }
            }
        } catch (_: Exception) {
            // fallback
        }

        return NetworkResult.Error(
            code = code,
            message = "Server error ($code): $errorBody"
        )
    }
}
