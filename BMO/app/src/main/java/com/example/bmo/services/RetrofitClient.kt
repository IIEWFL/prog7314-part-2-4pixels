package com.example.bmo.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    /*
    *GeekProbin. (2023a, August 19). React JS Gaming Website Tutorial With RAWG Video Games API | PART 1 | React For Beginners [Video]. YouTube. https://www.youtube.com/watch?v=TuOF8ppiKDY
    * endpoints and api https://api-docs.igdb.com/#getting-started
    * freeCodeCamp.org. (2025, June 10). MERN Stack Tutorial for Beginners with Deployment – 2025 [Video]. YouTube. https://www.youtube.com/watch?v=F9gB5b4jgOI
    */

    // Use your Render live URL
    private const val BASE_URL = "https://prog7314-part-2-4pixels.onrender.com/api/"

    val api: IgdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IgdbApiService::class.java)
    }
}
