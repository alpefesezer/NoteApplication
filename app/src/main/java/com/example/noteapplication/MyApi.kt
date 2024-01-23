package com.example.noteapplication
import retrofit2.Call
import retrofit2.http.GET

interface MyApi {
    @GET("random")
    fun getQuote(): Call<List<Quote>>
}