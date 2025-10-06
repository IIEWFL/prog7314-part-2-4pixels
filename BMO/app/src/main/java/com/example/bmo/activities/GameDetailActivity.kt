package com.example.bmo.activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bmo.R
import com.example.bmo.adapters.GameDetailAdapter
import com.example.bmo.models.Game
import com.example.bmo.services.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class GameDetailActivity : AppCompatActivity() {

    // https://firebase.google.com/docs/auth/android/start
    // Firebase Documentation
    // Used for implementing Firebase Authentication in Android

    // https://firebase.google.com/docs/database/android/start
    // Firebase Documentation
    // Used for setting up and using Firebase Realtime Database in Android
    private lateinit var imgCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var ratingBarDetail: android.widget.RatingBar
    private lateinit var tvRatingDetail: TextView
    private lateinit var tvRelease: TextView
    private lateinit var tvSummary: TextView
    private lateinit var tvGenres: TextView
    private lateinit var tvPlatforms: TextView
    private lateinit var tvDevelopers: TextView
    private lateinit var rvScreenshots: RecyclerView
    private lateinit var btnAddToLibrary: Button
    private lateinit var btnAddToFavourites: Button
    private lateinit var gameDetailAdapter: GameDetailAdapter

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private var currentGame: Game? = null
    private var isInLibrary = false
    private var isInFavourites = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)

        imgCover = findViewById(R.id.imgGameCover)
        tvTitle = findViewById(R.id.tvGameTitle)
        ratingBarDetail = findViewById(R.id.ratingBarDetail)
        tvRatingDetail = findViewById(R.id.tvRatingDetail)
        tvRelease = findViewById(R.id.tvGameRelease)
        tvSummary = findViewById(R.id.tvGameSummary)
        tvGenres = findViewById(R.id.tvGameGenres)
        tvPlatforms = findViewById(R.id.tvGamePlatforms)
        tvDevelopers = findViewById(R.id.tvGameDevelopers)
        rvScreenshots = findViewById(R.id.rvScreenshots)
        btnAddToLibrary = findViewById(R.id.btnAddToLibrary)
        btnAddToFavourites = findViewById(R.id.btnAddToFavourites)

        gameDetailAdapter = GameDetailAdapter(mutableListOf())
        rvScreenshots.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvScreenshots.adapter = gameDetailAdapter

        val gameId = intent.getIntExtra("GAME_ID", -1)
        if (gameId != -1) fetchGameDetails(gameId)
        else tvTitle.text = "Invalid Game ID"

        setupButtons()
    }

    private fun setupButtons() {
        btnAddToLibrary.setOnClickListener {
            currentGame?.let { game ->
                if (!isInLibrary) addToLibrary(game) else removeFromLibrary(game)
            }
        }

        btnAddToFavourites.setOnClickListener {
            currentGame?.let { game ->
                if (!isInFavourites) addToFavourites(game) else removeFromFavourites(game)
            }
        }
    }

    private fun fetchGameDetails(gameId: Int) {
        lifecycleScope.launch {
            try {
                val game: Game = RetrofitClient.api.getGameDetails(gameId)
                currentGame = game
                displayGameDetails(game)
                checkLibraryAndFavStatus(game)
            } catch (e: Exception) {
                Log.e("GameDetailActivity", "Failed to fetch game: ${e.message}")
                tvTitle.text = "Failed to load game"
            }
        }
    }

    private fun displayGameDetails(game: Game) {
        tvTitle.text = game.name
        val ratingOutOfFive = (game.rating?.toDouble() ?: 0.0) / 20.0
        ratingBarDetail.rating = ratingOutOfFive.toFloat()
        tvRatingDetail.text = String.format("%.1f", ratingOutOfFive)
        tvRelease.text = "Released: ${game.getFormattedReleaseDate()}"
        tvSummary.text = game.summary ?: "No summary available"
        tvGenres.text = "Genres: ${game.genres?.joinToString(", ") ?: "Unknown"}"
        tvPlatforms.text = "Platforms: ${game.platforms?.joinToString(", ") ?: "Unknown"}"
        tvDevelopers.text = "Developers: ${game.developers?.joinToString(", ") ?: "Unknown"}"

        Glide.with(this)
            .load(game.getCoverUrl())
            .placeholder(R.drawable.sample_game_cover)
            .error(R.drawable.sample_game_cover)
            .into(imgCover)

        gameDetailAdapter.setScreenshots(game.screenshots ?: emptyList())
    }

    private fun checkLibraryAndFavStatus(game: Game) {
        val uid = auth.currentUser?.uid ?: return

        // Library
        database.child("users").child(uid).child("library").child(game.id.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInLibrary = snapshot.exists()
                    updateLibraryButton()
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Favourites
        database.child("users").child(uid).child("favourites").child(game.id.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInFavourites = snapshot.exists()
                    updateFavouritesButton()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addToLibrary(game: Game) {
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).child("library").child(game.id.toString())
            .setValue(game)
            .addOnSuccessListener {
                isInLibrary = true
                updateLibraryButton()
            }
            .addOnFailureListener { e ->
                Log.e("GameDetailActivity", "Failed to add to library: ${e.message}")
            }
    }

    private fun removeFromLibrary(game: Game) {
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).child("library").child(game.id.toString())
            .removeValue()
            .addOnSuccessListener {
                isInLibrary = false
                updateLibraryButton()
            }
            .addOnFailureListener { e ->
                Log.e("GameDetailActivity", "Failed to remove from library: ${e.message}")
            }
    }

    private fun addToFavourites(game: Game) {
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).child("favourites").child(game.id.toString())
            .setValue(game)
            .addOnSuccessListener {
                isInFavourites = true
                updateFavouritesButton()
            }
            .addOnFailureListener { e ->
                Log.e("GameDetailActivity", "Failed to add to favourites: ${e.message}")
            }
    }

    private fun removeFromFavourites(game: Game) {
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).child("favourites").child(game.id.toString())
            .removeValue()
            .addOnSuccessListener {
                isInFavourites = false
                updateFavouritesButton()
            }
            .addOnFailureListener { e ->
                Log.e("GameDetailActivity", "Failed to remove from favourites: ${e.message}")
            }
    }

    private fun updateLibraryButton() {
        if (isInLibrary) {
            btnAddToLibrary.text = "Remove from Library"
            btnAddToLibrary.setBackgroundColor(Color.parseColor("#4CAF50"))
            btnAddToLibrary.setTextColor(Color.WHITE)
        } else {
            btnAddToLibrary.text = "Add to Library"
            btnAddToLibrary.setBackgroundColor(Color.parseColor("#6200EE"))
            btnAddToLibrary.setTextColor(Color.WHITE)
        }
    }

    private fun updateFavouritesButton() {
        if (isInFavourites) {
            btnAddToFavourites.text = "Remove from Favourites"
            btnAddToFavourites.setBackgroundColor(Color.parseColor("#4CAF50"))
            btnAddToFavourites.setTextColor(Color.WHITE)
        } else {
            btnAddToFavourites.text = "Add to Favourites"
            btnAddToFavourites.setBackgroundColor(Color.parseColor("#6200EE"))
            btnAddToFavourites.setTextColor(Color.WHITE)
        }
    }
}
