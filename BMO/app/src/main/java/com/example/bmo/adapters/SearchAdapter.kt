package com.example.bmo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bmo.R
import com.example.bmo.models.Game
import java.util.*

class SearchAdapter(
    val games: MutableList<Game> = mutableListOf(),
    private val layoutResId: Int = R.layout.item_list_game,
    private val onGameClick: ((Game) -> Unit)? = null
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return SearchViewHolder(view)

        // link: https://developer.android.com/guide/topics/ui/layout/recyclerview
        // website: Android Developers
        // used for: inflating RecyclerView item layout and creating ViewHolder
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val game = games[position]
        holder.tvTitle.text = game.name ?: "Unknown Game"

        val genre = game.genres?.joinToString(", ") ?: "Unknown Genre"
        val platform = game.platforms?.joinToString(", ") ?: "Unknown Platform"
        val year = game.first_release_date?.let { ((it * 1000L).let { Date(it) }.year + 1900).toString() } ?: "N/A"
        holder.tvInfo.text = "$genre • $platform • $year"

        // link: https://developer.android.com/reference/kotlin/java/util/Date
        // website: Android Developers
        // used for: converting Unix timestamp to year string for display

        // RatingBar
        val ratingOutOfFive = (game.rating?.toDouble() ?: 0.0) / 20.0
        holder.ratingBar.rating = ratingOutOfFive.toFloat()
        holder.tvRatingText.text = String.format("%.1f/5", ratingOutOfFive)

        Glide.with(holder.itemView.context)
            .load(game.getCoverUrl())
            .placeholder(R.drawable.sample_game_cover)
            .error(R.drawable.sample_game_cover)
            .into(holder.ivThumbnail)

        // link: https://developer.android.com/reference/kotlin/android/widget/RatingBar
        // website: Android Developers
        // used for: displaying game ratings visually and numerically

        holder.itemView.setOnClickListener { onGameClick?.invoke(game) }
    }

    override fun getItemCount(): Int = games.size

    // link: https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/RecyclerView.Adapter#getItemCount()
    // website: Android Developers
    // used for: returning the number of items in the adapter
    fun setGames(newGames: List<Game>) {
        games.clear()
        games.addAll(newGames)
        notifyDataSetChanged()
    }

    class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumbnail: ImageView = itemView.findViewById(R.id.ivGameThumbnail)
        val tvTitle: TextView = itemView.findViewById(R.id.tvGameTitle)
        val tvInfo: TextView = itemView.findViewById(R.id.tvGameInfo)
        val ratingBar: android.widget.RatingBar = itemView.findViewById(R.id.ratingBar)
        val tvRatingText: TextView = itemView.findViewById(R.id.tvRatingText)

        // link: https://developer.android.com/reference/kotlin/android/view/View#findViewById(androidx.annotation.IdRes)
        // website: Android Developers
        // used for: binding views from item layout to the ViewHolder
    }
}