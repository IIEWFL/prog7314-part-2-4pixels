package com.example.bmo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bmo.models.Game
import com.example.bmo.services.RetrofitClient
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val _allGames = MutableLiveData<List<Game>>()
    val allGames: LiveData<List<Game>> = _allGames

    private val _newReleases = MutableLiveData<List<Game>>()
    val newReleases: LiveData<List<Game>> = _newReleases

    private val _trendingGames = MutableLiveData<List<Game>>()
    val trendingGames: LiveData<List<Game>> = _trendingGames

    private val _searchResults = MutableLiveData<List<Game>>()
    val searchResults: LiveData<List<Game>> = _searchResults

    // Fetch all games (for filter/sort)
    fun fetchAllGames(force: Boolean = false) {
        if (force || _allGames.value.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val data = RetrofitClient.api.getAllGames()
                    _allGames.postValue(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun fetchNewReleases() {
        if (_newReleases.value.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val data = RetrofitClient.api.getNewReleases()
                    _newReleases.postValue(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun fetchTrendingGames() {
        if (_trendingGames.value.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val data = RetrofitClient.api.getTrendingGames()
                    _trendingGames.postValue(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun searchGames(query: String, limit: Int = 20) {
        viewModelScope.launch {
            try {
                val results = RetrofitClient.api.searchGames(query, limit)
                _searchResults.postValue(results)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
