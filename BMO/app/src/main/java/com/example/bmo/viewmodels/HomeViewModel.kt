package com.example.bmo.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bmo.models.Game
import com.example.bmo.services.RetrofitClient
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _trendingGames = MutableLiveData<List<Game>?>()
    val trendingGames: LiveData<List<Game>?> get() = _trendingGames

    private val _upcomingGames = MutableLiveData<List<Game>?>()
    val upcomingGames: LiveData<List<Game>?> get() = _upcomingGames

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    /**
     * Fetch trending games from the API with optional filters.
     * @param genres - list of genre IDs or names to filter by
     * @param platforms - list of platform IDs or names to filter by
     */
    fun fetchTrendingGames(
        genres: List<String>? = null,
        platforms: List<String>? = null
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getTrendingGames(
                    genres = genres?.joinToString(","),     // 👈 Convert list to comma-separated string
                    platforms = platforms?.joinToString(",") // 👈 Same here
                )
                _trendingGames.value = response
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to fetch trending games: ${e.message}")
                _trendingGames.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetch upcoming games from the API with optional filters.
     */
    fun fetchUpcomingGames(
        genres: List<String>? = null,
        platforms: List<String>? = null
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getUpcomingGames(
                    genres = genres?.joinToString(","),
                    platforms = platforms?.joinToString(",")
                )
                _upcomingGames.value = response
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to fetch upcoming games: ${e.message}")
                _upcomingGames.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
