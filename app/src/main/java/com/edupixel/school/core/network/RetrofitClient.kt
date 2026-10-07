package com.edupixel.school.core.network

import com.edupixel.school.core.config.ApiConfig
import com.edupixel.school.data.remote.EduPixelApiService
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
        prettyPrint = false
    }

    private val dynamicUrlInterceptor = Interceptor { chain ->
        var originalRequest = chain.request()
        val currentBaseUrl = ApiConfig.baseUrl.toHttpUrlOrNull()

        if (currentBaseUrl != null) {
            val newUrl = originalRequest.url.newBuilder()
                .scheme(currentBaseUrl.scheme)
                .host(currentBaseUrl.host)
                .port(currentBaseUrl.port)
                .build()
            originalRequest = originalRequest.newBuilder().url(newUrl).build()
        }

        chain.proceed(originalRequest)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(dynamicUrlInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val contentType = "application/json".toMediaType()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.DEFAULT_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val apiService: EduPixelApiService by lazy {
        retrofit.create(EduPixelApiService::class.java)
    }
}
