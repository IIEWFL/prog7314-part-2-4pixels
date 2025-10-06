package com.example.bmo.models

import java.text.SimpleDateFormat
import java.util.*

data class Game(
    val id: Int = 0,
    val name: String? = null,
    val rating: Float? = null,
    val first_release_date: Long? = null,
    val cover: Cover? = null,
    val banner: Banner? = null,
    val summary: String? = null,
    val screenshots: List<Screenshot>? = null,
    val genres: List<String>? = null,
    val platforms: List<String>? = null,
    val developers: List<String>? = null,
    val trailer: List<String>? = null,
    val makers: List<String>? = null
) {
    // Cover image URL for display
    fun getCoverUrl(): String? {
        return cover?.url?.let {
            if (it.startsWith("//")) "https:$it" else it
        }
    }

    // Formatted release date
    fun getFormattedReleaseDate(): String {
        return first_release_date?.let {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(it * 1000))
        } ?: "N/A"
    }
}

data class Cover(
    val id: Int? = null,
    val url: String? = null
)

data class Banner(
    val url: String? = null
)

data class Screenshot(
    val id: Int? = null,
    val url: String? = null
)
