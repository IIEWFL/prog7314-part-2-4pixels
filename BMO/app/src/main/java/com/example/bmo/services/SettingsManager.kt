package com.example.bmo.services

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

object SettingsManager {
    private const val PREFS_NAME = "user_settings"

    private var loaded = false
    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun loadSettingsFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().reference.child("users").child(uid).child("settings")
        try {
            val snapshot = dbRef.get().await()
            val genres = snapshot.child("selectedGenres").children.mapNotNull { it.getValue(String::class.java) }
            val platforms = snapshot.child("selectedPlatformsList").children.mapNotNull { it.getValue(String::class.java) }
            val sortOrder = snapshot.child("defaultSortOrder").getValue(String::class.java) ?: "Name"

            prefs.edit()
                .putStringSet("genres", genres.toSet())
                .putStringSet("platforms", platforms.toSet())
                .putString("sortOrder", sortOrder)
                .apply()

            loaded = true
            Log.d("SettingsManager", "Settings loaded successfully from Firebase")
        } catch (e: Exception) {
            Log.e("SettingsManager", "Failed to load settings: ${e.message}")
        }
    }

    fun getSelectedGenres(): List<String> {
        return prefs.getStringSet("genres", emptySet())?.toList() ?: emptyList()
    }

    fun getSelectedPlatforms(): List<String> {
        return prefs.getStringSet("platforms", emptySet())?.toList() ?: emptyList()
    }

    fun getSortOrder(): String {
        return prefs.getString("sortOrder", "Name") ?: "Name"
    }

    fun isReady(): Boolean = loaded
}
