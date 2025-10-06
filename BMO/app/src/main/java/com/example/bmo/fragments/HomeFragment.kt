package com.example.bmo.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bmo.R
import com.example.bmo.adapters.HomeAdapter
import com.example.bmo.models.Game
import com.example.bmo.services.RetrofitClient
import com.example.bmo.services.SettingsManager
import com.example.bmo.viewmodels.HomeViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * HomeFragment displays user-specific game information including
 * library stats, recommendations, and upcoming games.
 *
 * References:
 * 1. Firebase Realtime Database: https://firebase.google.com/docs/database/android/start
 * 2. Firebase Authentication: https://firebase.google.com/docs/auth/android/start
 * 3. RecyclerView: https://developer.android.com/guide/topics/ui/layout/recyclerview
 * 4. Glide: https://bumptech.github.io/glide/
 * 5. Retrofit: https://square.github.io/retrofit/
 * 6. Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
 * 7. MPAndroidChart: https://github.com/PhilJay/MPAndroidChart
 * 8. Android ViewModel & LiveData:
 *    https://developer.android.com/topic/libraries/architecture/viewmodel
 *    https://developer.android.com/topic/libraries/architecture/livedata
 */

class HomeFragment : Fragment() {

    private lateinit var layoutWelcome: View
    private lateinit var btnLoginSignup: Button
    private lateinit var btnAddGames: Button

    private lateinit var pieChart: PieChart
    private lateinit var rvRecommended: RecyclerView
    private lateinit var rvUpcoming: RecyclerView
    private lateinit var recommendedAdapter: HomeAdapter
    private lateinit var upcomingAdapter: HomeAdapter
    private lateinit var loadingIndicator: ProgressBar

    private lateinit var tvGenreStats: View
    private lateinit var tvRecommendations: View

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutWelcome = view.findViewById(R.id.layoutWelcome)
        btnLoginSignup = view.findViewById(R.id.btnLoginSignup)
        btnAddGames = view.findViewById(R.id.btnAddGames)

        pieChart = view.findViewById(R.id.pieChartGenres)
        rvRecommended = view.findViewById(R.id.rvRecommendations)
        rvUpcoming = view.findViewById(R.id.rvUpcoming)
        loadingIndicator = view.findViewById(R.id.progressLoadingHome)

        tvGenreStats = view.findViewById(R.id.tvGenreStats)
        tvRecommendations = view.findViewById(R.id.tvRecommendations)

        setupRecyclerView(rvRecommended).also { recommendedAdapter = it }
        setupRecyclerView(rvUpcoming).also { upcomingAdapter = it }

        setupObservers()

        btnLoginSignup.setOnClickListener {
            startActivity(
                android.content.Intent(requireContext(), com.example.bmo.activities.RegisterActivity::class.java)
            )
        }

        btnAddGames.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, com.example.bmo.fragments.SearchFragment())
                .addToBackStack(null)
                .commit()
        }

        CoroutineScope(Dispatchers.Main).launch {
            loadingIndicator.visibility = View.VISIBLE
            SettingsManager.loadSettingsFromFirebase()
            checkUserLibraryAndSetupUI()
        }
    }
    /**
     * Check if user is logged in and has games in their library.
     * Updates UI accordingly.
     *
     * References:
     * - Firebase Realtime Database: https://firebase.google.com/docs/database/android/start
     * - Firebase Authentication: https://firebase.google.com/docs/auth/android/start
     */
    private fun checkUserLibraryAndSetupUI() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            // Not logged in → show welcome/login
            layoutWelcome.visibility = View.VISIBLE
            btnLoginSignup.visibility = View.VISIBLE
            btnAddGames.visibility = View.GONE

            pieChart.visibility = View.GONE
            rvRecommended.visibility = View.GONE
            tvGenreStats.visibility = View.GONE
            tvRecommendations.visibility = View.GONE
            rvUpcoming.visibility = View.VISIBLE // upcoming games still visible

            applySettingsAndFetchData()
            loadingIndicator.visibility = View.GONE
            return
        }

        // Logged in → check if library has games
        val dbRef = FirebaseDatabase.getInstance().reference
            .child("users").child(currentUser.uid).child("library")

        dbRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    // No games → show "Add Games" button
                    layoutWelcome.visibility = View.VISIBLE
                    btnLoginSignup.visibility = View.GONE
                    btnAddGames.visibility = View.VISIBLE

                    pieChart.visibility = View.GONE
                    rvRecommended.visibility = View.GONE
                    tvGenreStats.visibility = View.GONE
                    tvRecommendations.visibility = View.GONE
                } else {
                    // Has games → show pie chart & recommendations
                    layoutWelcome.visibility = View.GONE

                    pieChart.visibility = View.VISIBLE
                    tvGenreStats.visibility = View.VISIBLE

                    rvRecommended.visibility = View.VISIBLE
                    tvRecommendations.visibility = View.VISIBLE

                    setupPieChart()
                    fetchRecommendedGames()
                }

                // Upcoming games visible in all logged-in states
                rvUpcoming.visibility = View.VISIBLE
                applySettingsAndFetchData()
                loadingIndicator.visibility = View.GONE
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                loadingIndicator.visibility = View.GONE
            }
        })
    }

    private fun applySettingsAndFetchData() {
        val selectedGenres = SettingsManager.getSelectedGenres()
        val selectedPlatforms = SettingsManager.getSelectedPlatforms()
        val sortOrder = SettingsManager.getSortOrder()

        viewModel.fetchUpcomingGames(selectedGenres, selectedPlatforms)

        viewModel.upcomingGames.observe(viewLifecycleOwner) { games ->
            upcomingAdapter.setGames(applySortOrder(games ?: emptyList(), sortOrder))
        }
    }

    private fun applySortOrder(games: List<Game>, sortOrder: String): List<Game> {
        return when (sortOrder.lowercase()) {
            "rating" -> games.sortedByDescending { it.rating ?: 0f }
            "release date" -> games.sortedByDescending { it.first_release_date ?: 0L }
            "alphabetical", "name" -> games.sortedBy { it.name ?: "" }
            else -> games
        }
    }

    /**
     * Apply user-selected genres, platforms, and sort order.
     * Fetch upcoming games accordingly.
     *
     * References:
     * - Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
     */
    private fun setupPieChart() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val dbRef = FirebaseDatabase.getInstance().reference
            .child("users").child(currentUser.uid).child("library")

        dbRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val genreCount = mutableMapOf<String, Int>()

                for (child in snapshot.children) {
                    val game = child.getValue(Game::class.java)
                    game?.genres?.forEach { genre ->
                        genreCount[genre] = (genreCount[genre] ?: 0) + 1
                    }
                }

                if (genreCount.isEmpty()) {
                    pieChart.visibility = View.GONE
                    tvGenreStats.visibility = View.GONE
                    return
                } else {
                    pieChart.visibility = View.VISIBLE
                    tvGenreStats.visibility = View.VISIBLE
                }

                val total = genreCount.values.sum().toFloat()
                val entries = genreCount.map { (genre, count) ->
                    PieEntry(count / total * 100f, genre)
                }

                val colors = listOf(
                    Color.parseColor("#4ABDAC"),
                    Color.parseColor("#FC4A1A"),
                    Color.parseColor("#F7B32B"),
                    Color.parseColor("#3BB273"),
                    Color.parseColor("#9B59B6"),
                    Color.parseColor("#E67E22"),
                    Color.parseColor("#3498DB")
                )

                val dataSet = PieDataSet(entries, "").apply {
                    setColors(colors)
                    valueTextColor = Color.WHITE
                    valueTextSize = 14f
                }

                pieChart.apply {
                    data = PieData(dataSet)
                    description.isEnabled = false
                    setUsePercentValues(true)
                    setDrawEntryLabels(false)
                    setDrawHoleEnabled(true)
                    holeRadius = 40f
                    invalidate()
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                pieChart.visibility = View.GONE
                tvGenreStats.visibility = View.GONE
            }
        })
    }

    /**
     * Fetch recommended games based on user library genres using Retrofit API
     *
     * References:
     * - Retrofit: https://square.github.io/retrofit/
     * - Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
     */

    private fun fetchRecommendedGames() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val dbRef = FirebaseDatabase.getInstance().reference
            .child("users").child(currentUser.uid).child("library")

        dbRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val genresSet = mutableSetOf<String>()
                for (child in snapshot.children) {
                    val game = child.getValue(Game::class.java)
                    game?.genres?.let { genresSet.addAll(it) }
                }

                if (genresSet.isEmpty()) {
                    recommendedAdapter.setGames(emptyList())
                    tvRecommendations.visibility = View.GONE
                    return
                }

                tvRecommendations.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val popularGames = RetrofitClient.api.getPopularGames(
                            limit = 20,
                            genres = genresSet.joinToString(",")
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            recommendedAdapter.setGames(popularGames)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    private fun setupRecyclerView(rv: RecyclerView): HomeAdapter {
        val adapter = HomeAdapter(mutableListOf()) { game -> openGameDetail(game) }
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = adapter
        return adapter
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            loadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun openGameDetail(game: Game) {
        val intent = android.content.Intent(requireContext(), com.example.bmo.activities.GameDetailActivity::class.java)
        intent.putExtra("GAME_ID", game.id)
        startActivity(intent)
    }
}
