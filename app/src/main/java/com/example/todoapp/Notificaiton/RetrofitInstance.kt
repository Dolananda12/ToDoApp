package com.example.todoapp.Notificaiton

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance {
    companion object{
          val BASE_URL="https://todoapp-dsrinaga.p.tnnl.in"
          val interceptor =HttpLoggingInterceptor().apply {
                 this.level=HttpLoggingInterceptor.Level.BODY
          }
        val gson = GsonBuilder().setLenient().create()
        val client =OkHttpClient.Builder().apply {
                 this.addInterceptor(interceptor)
                     .connectTimeout(30,TimeUnit.SECONDS)
                     .readTimeout(20,TimeUnit.SECONDS)
                     .writeTimeout(10,TimeUnit.SECONDS)
                 }.build()
          fun getRetrofitInstance() : Retrofit{
                return Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create(gson)).build()
          }
    }
}

