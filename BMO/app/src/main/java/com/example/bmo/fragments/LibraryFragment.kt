package com.example.bmo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bmo.R
import com.example.bmo.adapters.LibraryAdapter
import com.example.bmo.models.Game
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

/**
 * LibraryFragment displays the user's personal game library.
 * Supports sorting and filtering by favourites, name, rating, or release date.
 *
 * References:
 * 1. Firebase Realtime Database: https://firebase.google.com/docs/database/android/start
 * 2. Firebase Authentication: https://firebase.google.com/docs/auth/android/start
 * 3. RecyclerView & Adapter: https://developer.android.com/guide/topics/ui/layout/recyclerview
 * 4. Android AlertDialog: https://developer.android.com/guide/topics/ui/dialogs
 * 5. Kotlin Collections: https://kotlinlang.org/docs/collections-overview.html
 */

class LibraryFragment : Fragment() {

    private lateinit var rvLibrary: RecyclerView
    private lateinit var tvNoLibrary: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnSort: Button
    private lateinit var btnFilter: Button

    private lateinit var libraryAdapter: LibraryAdapter

    private var allGames = mutableListOf<Game>()
    private val favouriteGameIds = mutableSetOf<Int>()

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // Sorting & filtering state
    private var sortMode: SortMode = SortMode.NONE
    private var filterMode: FilterMode = FilterMode.ALL

    enum class SortMode { NONE, NAME, RATING, RELEASE }
    enum class FilterMode { ALL, FAVOURITES }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_library, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvLibrary = view.findViewById(R.id.rvLibrary)
        tvNoLibrary = view.findViewById(R.id.tvNoLibrary)
        tvStatus = view.findViewById(R.id.tvStatus)
        btnSort = view.findViewById(R.id.btnSort)
        btnFilter = view.findViewById(R.id.btnFilter)

        libraryAdapter = LibraryAdapter(
            mutableListOf(),
            favourites = favouriteGameIds,
            onGameClick = { game -> openGameDetail(game) },
            onEmptyList = { isEmpty -> tvNoLibrary.visibility = if (isEmpty) View.VISIBLE else View.GONE }
        )

        rvLibrary.layoutManager = LinearLayoutManager(requireContext())
        rvLibrary.adapter = libraryAdapter

        setupButtons()
        fetchFavouritesAndLibrary()
    }

    /**
     * Setup sorting and filtering buttons using AlertDialog.
     *
     * Reference:
     * - Android AlertDialog: https://developer.android.com/guide/topics/ui/dialogs
     */
    private fun setupButtons() {
        btnSort.setOnClickListener {
            val options = arrayOf("None", "Name", "Rating", "Release Date")
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Sort by")
                .setItems(options) { _, which ->
                    sortMode = when (which) {
                        1 -> SortMode.NAME
                        2 -> SortMode.RATING
                        3 -> SortMode.RELEASE
                        else -> SortMode.NONE
                    }
                    applySortAndFilter()
                }.show()
        }

        btnFilter.setOnClickListener {
            val options = arrayOf("All Games", "Favourites Only")
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Filter")
                .setItems(options) { _, which ->
                    filterMode = if (which == 1) FilterMode.FAVOURITES else FilterMode.ALL
                    applySortAndFilter()
                }.show()
        }
    }

    /**
     * Apply sorting and filtering to the library games list.
     *
     * Reference:
     * - Kotlin Collections: https://kotlinlang.org/docs/collections-overview.html
     */
    private fun applySortAndFilter() {
        var filtered = allGames.toList()

        // Apply filter
        if (filterMode == FilterMode.FAVOURITES) {
            filtered = filtered.filter { favouriteGameIds.contains(it.id) }
        }

        // Apply sort
        filtered = when (sortMode) {
            SortMode.NAME -> filtered.sortedBy { it.name ?: "" }
            SortMode.RATING -> filtered.sortedByDescending { it.rating?.toDouble() ?: 0.0 }
            SortMode.RELEASE -> filtered.sortedByDescending { it.first_release_date ?: 0L }
            else -> filtered
        }

        libraryAdapter.setGames(filtered)

        // Show status
        tvStatus.visibility = View.VISIBLE
        val status = mutableListOf<String>()
        if (filterMode == FilterMode.FAVOURITES) status.add("Favourites Only")
        when (sortMode) {
            SortMode.NAME -> status.add("Sorted by Name")
            SortMode.RATING -> status.add("Sorted by Rating")
            SortMode.RELEASE -> status.add("Sorted by Release")
            else -> {}
        }
        tvStatus.text = status.joinToString(" • ").ifEmpty { "" }
        tvStatus.visibility = if (status.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun fetchFavouritesAndLibrary() {
        val uid = auth.currentUser?.uid ?: return

        database.child("users").child(uid).child("favourites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    favouriteGameIds.clear()
                    for (child in snapshot.children) {
                        child.key?.toIntOrNull()?.let { favouriteGameIds.add(it) }
                    }
                    fetchLibraryGames()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /**
     * Fetch user's library games from Firebase.
     *
     * References:
     * - Firebase Realtime Database: https://firebase.google.com/docs/database/android/start
     */

    private fun fetchLibraryGames() {
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).child("library")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allGames.clear()
                    for (child in snapshot.children) {
                        child.getValue(Game::class.java)?.let { allGames.add(it) }
                    }
                    applySortAndFilter() // Show updated library with sort/filter
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /**
     * Open GameDetailActivity for a selected game.
     *
     * References:
     * - Android Intent: https://developer.android.com/guide/components/intents-filters
     */
    private fun openGameDetail(game: Game) {
        val intent = android.content.Intent(requireContext(), com.example.bmo.activities.GameDetailActivity::class.java)
        intent.putExtra("GAME_ID", game.id)
        startActivity(intent)
    }
}
