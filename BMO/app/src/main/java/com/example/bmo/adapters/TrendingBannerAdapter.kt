package com.example.bmo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bmo.R
import com.example.bmo.models.Game

class TrendingBannerAdapter(
    private val games: List<Game>,
    private val onGameClick: (Game) -> Unit
) : RecyclerView.Adapter<TrendingBannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner_game, parent, false)
        return BannerViewHolder(view)

        // link: https://developer.android.com/guide/topics/ui/layout/recyclerview
        // website: Android Developers
        // used for: inflating RecyclerView banner item layout and creating ViewHolder
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val game = games[position]
        Glide.with(holder.itemView.context)
            .load(game.getCoverUrl())
            .placeholder(R.drawable.sample_game_cover)
            .into(holder.ivBanner)

        holder.itemView.setOnClickListener { onGameClick(game) }

        // link: https://github.com/bumptech/glide
        // website: Glide GitHub Repository
        // used for: asynchronous image loading from URLs with placeholder
    }

    override fun getItemCount(): Int = games.size

    // link: https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/RecyclerView.Adapter#getItemCount()
    // website: Android Developers
    // used for: returning the total number of items in the adapter
    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBanner: ImageView = itemView.findViewById(R.id.ivBannerGame)

        // link: https://developer.android.com/reference/kotlin/android/view/View#findViewById(androidx.annotation.IdRes)
        // website: Android Developers
        // used for: binding banner image view from the layout to the ViewHolder
    }
}
