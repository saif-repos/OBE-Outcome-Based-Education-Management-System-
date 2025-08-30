//package com.example.obe_mngt_sys.HELPER
//
//import com.example.obe_mngt_sys.SERVICES.ApiService
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object RetrofitInstance {
//    private const val BASE_URL = "http://192.168.134.115/api_obe/"
//
//    val apiService: ApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ApiService::class.java)
//
//    }
//}
//

package com.example.obe_mngt_sys.HELPER

import com.example.obe_mngt_sys.SERVICES.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit // Don't forget this import

object RetrofitInstance {
    private const val BASE_URL = "http://192.168.100.66/api_obe/"

    // Create an OkHttpClient with custom timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Connect timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Set the custom OkHttpClient here
            .build()
            .create(ApiService::class.java)
    }
}