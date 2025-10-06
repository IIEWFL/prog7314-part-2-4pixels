package com.example.bmo.models

data class GameSearchResponse(
    val count: Int,
    val results: List<Game>
)