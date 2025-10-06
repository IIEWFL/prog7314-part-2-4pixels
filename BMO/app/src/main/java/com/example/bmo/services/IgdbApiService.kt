package com.example.bmo.services

import com.example.bmo.models.Game
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IgdbApiService {

    @GET("igdb/trending")
    suspend fun getTrendingGames(
        @Query("genres") genres: String? = null,
        @Query("platforms") platforms: String? = null,
        @Query("limit") limit: Int = 20
    ): List<Game>

    @GET("igdb/popular") // NEW endpoint for popular games
    suspend fun getPopularGames(
        @Query("genres") genres: String? = null,
        @Query("platforms") platforms: String? = null,
        @Query("limit") limit: Int = 20
    ): List<Game>

    @GET("igdb/search")
    suspend fun searchGames(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): List<Game>

    @GET("games/{id}")
    suspend fun getGameDetails(@Path("id") id: Int): Game

    @GET("igdb/upcoming")
    suspend fun getUpcomingGames(
        @Query("genres") genres: String? = null,
        @Query("platforms") platforms: String? = null,
        @Query("limit") limit: Int = 10
    ): List<Game>

    @GET("igdb/new-releases")
    suspend fun getNewReleases(
        @Query("limit") limit: Int = 10
    ): List<Game>

    @GET("igdb/all-games")
    suspend fun getAllGames(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("genres") genres: String? = null,
        @Query("platforms") platforms: String? = null
    ): List<Game>
}
