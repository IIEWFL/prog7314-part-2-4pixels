package com.example.bmo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bmo.R
import com.example.bmo.models.Game
import java.text.SimpleDateFormat
import java.util.*

class LibraryAdapter(
    private val games: MutableList<Game> = mutableListOf(),
    private val favourites: Set<Int> = emptySet(),
    private val onGameClick: ((Game) -> Unit)? = null,
    private val onEmptyList: ((Boolean) -> Unit)? = null
) : RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_game, parent, false)
        return LibraryViewHolder(view)
    }
    // link: https://developer.android.com/guide/topics/ui/layout/recyclerview
    // website: Android Developers
    // used for: inflating a RecyclerView item layout and creating a ViewHolder

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        val game = games[position]

        holder.tvTitle.text = game.name ?: "Unknown Game"

        // Safe division: convert nullable rating to Double, default to 0.0
        val ratingOutOfFive = (game.rating?.toDouble() ?: 0.0) / 20.0
        holder.ratingBar.rating = ratingOutOfFive.toFloat()
        holder.tvRatingText.text = String.format("%.1f", ratingOutOfFive)

        holder.tvGameInfo.text = buildGameInfo(game)

        Glide.with(holder.itemView.context)
            .load(game.getCoverUrl())
            .placeholder(R.drawable.sample_game_cover)
            .error(R.drawable.sample_game_cover)
            .into(holder.ivThumbnail)

        holder.ivFavourite.visibility = if (favourites.contains(game.id)) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onGameClick?.invoke(game) }
    }
    // link: https://developer.android.com/reference/kotlin/android/widget/RatingBar
    // website: Android Developers
    // used for: displaying game ratings as stars

    override fun getItemCount(): Int = games.size

    fun setGames(newGames: List<Game>) {
        games.clear()
        games.addAll(newGames)
        notifyDataSetChanged()
        onEmptyList?.invoke(games.isEmpty())
    }
    // link: https://developer.android.com/reference/kotlin/android/view/View#setVisibility(int)
    // website: Android Developers
    // used for: toggling favourite icon visibility
    private fun buildGameInfo(game: Game): String {
        val genre = game.genres?.joinToString(", ") ?: "Unknown Genre"
        val platform = game.platforms?.joinToString(", ") ?: "Unknown Platform"
        val year = game.first_release_date?.let {
            SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it * 1000))
        } ?: "N/A"
        return "$genre • $platform • $year"
    }

    class LibraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumbnail: ImageView = itemView.findViewById(R.id.ivGameThumbnail)
        val tvTitle: TextView = itemView.findViewById(R.id.tvGameTitle)
        val tvGameInfo: TextView = itemView.findViewById(R.id.tvGameInfo)
        val ivFavourite: ImageView = itemView.findViewById(R.id.ivFavourite)

        // link: https://developer.android.com/reference/kotlin/android/view/View#findViewById(androidx.annotation.IdRes)
        // website: Android Developers
        // used for: binding item layout views to the ViewHolder properties
        val ratingBar: android.widget.RatingBar = itemView.findViewById(R.id.ratingBar)
        val tvRatingText: TextView = itemView.findViewById(R.id.tvRatingText)
    }
}
