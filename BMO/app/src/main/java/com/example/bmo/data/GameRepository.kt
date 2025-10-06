package com.example.bmo.data

import com.example.bmo.models.Game

object GameRepository {
    private val favouriteGames = mutableListOf<Game>()
    private val libraryGames = mutableListOf<Game>()

    fun addToFavourites(game: Game) {
        if (!favouriteGames.any { it.id == game.id }) {
            favouriteGames.add(game)
        }
    }
    // link: https://kotlinlang.org/docs/collections-overview.html#any
    // website: Kotlin Documentation
    // used for: checking if the game already exists before adding to favourites

    fun addToLibrary(game: Game) {
        if (!libraryGames.any { it.id == game.id }) {
            libraryGames.add(game)

            // link: https://kotlinlang.org/docs/collections-overview.html#any
            // website: Kotlin Documentation
            // used for: checking if the game already exists before adding to library
        }
    }

    fun getFavourites(): List<Game> = favouriteGames
    // link: https://kotlinlang.org/docs/collections-overview.html#any
    // website: Kotlin Documentation
    // used for: checking if the game already exists before adding to library

    fun getLibrary(): List<Game> = libraryGames
}
