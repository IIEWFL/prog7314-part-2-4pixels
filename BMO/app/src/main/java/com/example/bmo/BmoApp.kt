package com.example.bmo

import android.app.Application
import com.example.bmo.services.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BmoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize SettingsManager for global access
        SettingsManager.init(this)

        // Load user settings (if logged in)
        CoroutineScope(Dispatchers.IO).launch {
            SettingsManager.loadSettingsFromFirebase()
        }
    }
}
