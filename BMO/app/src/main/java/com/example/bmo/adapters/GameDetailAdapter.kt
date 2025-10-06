package com.example.bmo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bmo.R
import com.example.bmo.models.Screenshot

class GameDetailAdapter(
    private val screenshots: MutableList<Screenshot> = mutableListOf()
) : RecyclerView.Adapter<GameDetailAdapter.DetailViewHolder>() {

    // link: https://developer.android.com/guide/topics/ui/layout/recyclerview
    // website: Android Developers
    // used for: inflating layout for RecyclerView item and creating ViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_screenshot, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val screenshot = screenshots[position]

        Glide.with(holder.itemView.context)
            .load(screenshot.url)
            .placeholder(R.drawable.sample_game_cover)
            .error(R.drawable.sample_game_cover)
            .into(holder.imgScreenshot)
    }
    // link: https://github.com/bumptech/glide
    // website: Glide GitHub Repository
    // used for: loading images asynchronously from URL into ImageView with placeholder/error handling

    override fun getItemCount(): Int = screenshots.size

    fun setScreenshots(newScreenshots: List<Screenshot>) {
        screenshots.clear()
        screenshots.addAll(newScreenshots)
        notifyDataSetChanged()
    }
    // link: https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/RecyclerView.Adapter#notifyDataSetChanged()
    // website: Android Developers
    // used for: updating the adapter's data and refreshing the RecyclerView display
    class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgScreenshot: ImageView = itemView.findViewById(R.id.imgScreenshot)
    }
}
