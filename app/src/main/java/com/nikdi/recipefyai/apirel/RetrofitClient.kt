package com.nikdi.recipefyai.apirel

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

object RetrofitClient {
    private const val REQ_URL = "https://noitrecipefai.duckdns.org/"

    val apiService: RecipeRequester by lazy {
        Retrofit.Builder()
            .baseUrl(REQ_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeRequester::class.java)
    }
}