package com.example.bmo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bmo.R
import com.example.bmo.models.Game

class HomeAdapter(
    private val games: MutableList<Game> = mutableListOf(),
    private val layoutResId: Int = R.layout.item_game_recommendation,
    private val imageViewId: Int = R.id.imgGame,
    private val titleTextViewId: Int = R.id.tvGameTitle,
    private val onGameClick: ((Game) -> Unit)? = null
) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return HomeViewHolder(view, imageViewId, titleTextViewId)
    }
    // link: https://developer.android.com/guide/topics/ui/layout/recyclerview
    // website: Android Developers
    // used for: inflating the layout for RecyclerView items and creating a ViewHolder

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val game = games[position]
        holder.tvTitle.text = game.name ?: "Unknown Game"

        Glide.with(holder.itemView.context)
            .load(game.getCoverUrl())
            .placeholder(R.drawable.sample_game_cover)
            .error(R.drawable.sample_game_cover)
            .into(holder.ivThumbnail)

        holder.itemView.setOnClickListener { onGameClick?.invoke(game) }
    }
    // link: https://github.com/bumptech/glide
    // website: Glide GitHub Repository
    // used for: loading images asynchronously from URLs into ImageViews with placeholders/error handling

    override fun getItemCount(): Int = games.size
    // link: https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/RecyclerView.Adapter#getItemCount()
    // website: Android Developers
    // used for: returning the total number of items in the adapter

    fun setGames(newGames: List<Game>) {
        games.clear()
        games.addAll(newGames)
        notifyDataSetChanged()
    }

    // link: https://developer.android.com/reference/kotlin/android/view/View#findViewById(androidx.annotation.IdRes)
    // website: Android Developers
    // used for: binding views from the item layout to the ViewHolder
    class HomeViewHolder(itemView: View, imageViewId: Int, titleTextViewId: Int) :
        RecyclerView.ViewHolder(itemView) {
        val ivThumbnail: ImageView = itemView.findViewById(imageViewId)
        val tvTitle: TextView = itemView.findViewById(titleTextViewId)
    }
}
