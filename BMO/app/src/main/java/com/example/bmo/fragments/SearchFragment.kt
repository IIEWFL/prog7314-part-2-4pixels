package com.example.bmo.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.bmo.R
import com.example.bmo.adapters.SearchAdapter
import com.example.bmo.adapters.TrendingBannerAdapter
import com.example.bmo.models.Game
import com.example.bmo.services.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SearchFragment allows users to browse, search, and filter games.
 * Displays trending banners, new releases, and search results.
 *
 * References:
 * 1. Retrofit API calls: https://square.github.io/retrofit/
 * 2. RecyclerView & Adapter: https://developer.android.com/guide/topics/ui/layout/recyclerview
 * 3. ViewPager2 for banners: https://developer.android.com/reference/androidx/viewpager2/widget/ViewPager2
 * 4. SearchView usage: https://developer.android.com/reference/androidx/appcompat/widget/SearchView
 * 5. AlertDialog for multi-choice filters: https://developer.android.com/guide/topics/ui/dialogs
 * 6. Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
 * 7. Handler for auto-scrolling banners: https://developer.android.com/reference/android/os/Handler
 */
class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var rvGamesList: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var trendingBannerContainer: View
    private lateinit var trendingViewPager: ViewPager2
    private lateinit var tvNewReleases: TextView
    private lateinit var btnFilter: Button
    private lateinit var tvSearchStatus: TextView

    private var searchJob: Job? = null
    private lateinit var bannerHandler: Handler
    private lateinit var bannerRunnable: Runnable

    // Current state
    private var currentGames: List<Game> = emptyList()
    private var currentGenres: MutableList<String> = mutableListOf()

    // IGDB-supported genres
    private val allGenres = arrayOf(
        "Pinball", "Adventure", "Indie", "Arcade", "Visual Novel",
        "Card & Board Game", "MOBA", "Point-and-click", "Fighting",
        "Shooter", "Music", "Platform", "Puzzle", "Racing",
        "Real Time Strategy (RTS)", "Role-playing (RPG)", "Simulator",
        "Sport", "Strategy", "Turn-based strategy (TBS)", "Tactical",
        "Hack and slash/Beat 'em up", "Quiz/Trivia"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Initialize views ---
        searchView = view.findViewById(R.id.searchView)
        rvGamesList = view.findViewById(R.id.rvGamesList)
        trendingBannerContainer = view.findViewById(R.id.trendingBannerContainer)
        trendingViewPager = view.findViewById(R.id.vpTrendingBanner)
        loadingIndicator = view.findViewById(R.id.progressLoading)
        tvNewReleases = view.findViewById(R.id.tvNewReleases)
        btnFilter = view.findViewById(R.id.btnFilter)
        tvSearchStatus = view.findViewById(R.id.tvSearchStatus)

        bannerHandler = Handler(Looper.getMainLooper())
        bannerRunnable = Runnable {
            val nextItem: Int = (trendingViewPager.currentItem + 1) % (trendingViewPager.adapter?.itemCount ?: 1)
            trendingViewPager.setCurrentItem(nextItem, true)
            bannerHandler.postDelayed(bannerRunnable, 5000)
        }

        setupRecyclerView()
        fetchNewReleases()
        fetchTrendingGames()
        setupSearchListener()
        setupFilterButton()
    }

    /**
     * Setup RecyclerView with SearchAdapter.
     *
     * References:
     * - RecyclerView: https://developer.android.com/guide/topics/ui/layout/recyclerview
     */
    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(mutableListOf()) { game -> openGameDetail(game) }
        rvGamesList.layoutManager = LinearLayoutManager(requireContext())
        rvGamesList.adapter = searchAdapter
        rvGamesList.isNestedScrollingEnabled = false
    }

    /**
     * Fetch new releases from the API.
     *
     * References:
     * - Retrofit API calls: https://square.github.io/retrofit/
     */
    private fun fetchNewReleases() {
        lifecycleScope.launch {
            try {
                loadingIndicator.visibility = View.VISIBLE
                val newReleases: List<Game> = RetrofitClient.api.getNewReleases()
                currentGames = newReleases
                applyFilterInternal(newReleases)
                trendingBannerContainer.visibility = View.VISIBLE
                tvNewReleases.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("SearchFragment", "Failed to fetch new releases: ${e.message}")
            } finally {
                loadingIndicator.visibility = View.GONE
            }
        }
    }
    /**
     * Fetch trending games for the banner.
     *
     * References:
     * - ViewPager2: https://developer.android.com/reference/androidx/viewpager2/widget/ViewPager2
     */

    private fun fetchTrendingGames() {
        lifecycleScope.launch {
            try {
                val trendingGames: List<Game> = RetrofitClient.api.getTrendingGames()
                trendingBannerContainer.visibility = View.VISIBLE
                trendingViewPager.adapter = TrendingBannerAdapter(trendingGames) { openGameDetail(it) }

                bannerHandler.removeCallbacks(bannerRunnable)
                bannerHandler.postDelayed(bannerRunnable, 5000)

                trendingViewPager.setPageTransformer { page, position ->
                    page.translationX = -30f * position
                    page.scaleY = 0.85f + (1 - kotlin.math.abs(position)) * 0.15f
                }
            } catch (e: Exception) {
                Log.e("SearchFragment", "Failed to fetch trending: ${e.message}")
                trendingBannerContainer.visibility = View.GONE
            }
        }
    }

    /**
     * Setup SearchView query listener.
     *
     * References:
     * - SearchView: https://developer.android.com/reference/androidx/appcompat/widget/SearchView
     */
    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim().orEmpty()
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)
                    if (query.isEmpty() && currentGenres.isEmpty()) {
                        fetchNewReleases()
                    } else {
                        trendingBannerContainer.visibility = View.GONE
                        tvNewReleases.visibility = View.GONE
                        if (currentGenres.isNotEmpty()) {
                            fetchAllGamesWithFilter(currentGenres)
                        } else if (query.isNotEmpty()) {
                            searchGames(query)
                        }
                    }
                }
                return true
            }
        })
    }

    private suspend fun searchGames(query: String) {
        try {
            loadingIndicator.visibility = View.VISIBLE
            val results: List<Game> = RetrofitClient.api.searchGames(query, limit = 20)
            currentGames = results
            applyFilterInternal(results)
        } catch (e: Exception) {
            Log.e("SearchFragment", "Search failed: ${e.message}")
        } finally {
            loadingIndicator.visibility = View.GONE
        }
    }

    private fun setupFilterButton() {
        btnFilter.setOnClickListener { showFilterDialog() }
    }

    /**
     * Show multi-choice genre filter dialog.
     *
     * References:
     * - AlertDialog: https://developer.android.com/guide/topics/ui/dialogs
     */
    private fun showFilterDialog() {
        val selectedGenres = BooleanArray(allGenres.size) { currentGenres.contains(allGenres[it]) }

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Genre")
            .setMultiChoiceItems(allGenres, selectedGenres) { _, which, isChecked ->
                if (isChecked) currentGenres.add(allGenres[which])
                else currentGenres.remove(allGenres[which])
            }
            .setPositiveButton("Apply") { _, _ ->
                if (currentGenres.isEmpty()) fetchNewReleases()
                else fetchAllGamesWithFilter(currentGenres)
            }
            .setNegativeButton("Clear") { _, _ ->
                currentGenres.clear()
                fetchNewReleases()
            }
            .show()
    }

    private fun fetchAllGamesWithFilter(genres: List<String>) {
        lifecycleScope.launch {
            try {
                loadingIndicator.visibility = View.VISIBLE
                trendingBannerContainer.visibility = View.GONE
                tvNewReleases.visibility = View.GONE

                val genreQuery = genres.joinToString(",")
                val filteredGames: List<Game> = RetrofitClient.api.getAllGames(
                    limit = 50,
                    offset = 0,
                    genres = genreQuery
                )
                currentGames = filteredGames
                applyFilterInternal(filteredGames)
            } catch (e: Exception) {
                Log.e("SearchFragment", "Failed to fetch filtered games: ${e.message}")
            } finally {
                loadingIndicator.visibility = View.GONE
            }
        }
    }

    private fun applyFilterInternal(list: List<Game>) {
        searchAdapter.setGames(list)

        // Update filter status
        val status = if (currentGenres.isNotEmpty()) {
            "Filtered by: ${currentGenres.joinToString(", ")}"
        } else {
            ""
        }

        tvSearchStatus.text = status
        tvSearchStatus.visibility = if (status.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun openGameDetail(game: Game) {
        val intent = Intent(requireContext(), com.example.bmo.activities.GameDetailActivity::class.java)
        intent.putExtra("GAME_ID", game.id)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerHandler.removeCallbacks(bannerRunnable)
    }
}
