package com.example.bmo.activities

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bmo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // UI references
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var switchGenres: Switch
    private lateinit var switchPlatforms: Switch
    private lateinit var spinnerSortOrder: Spinner
    private lateinit var btnLogout: Button

    // Genre selection
    private val allGenres = arrayOf(
        "Pinball", "Adventure", "Indie", "Arcade", "Visual Novel",
        "Card & Board Game", "MOBA", "Point-and-click", "Fighting",
        "Shooter", "Music", "Platform", "Puzzle", "Racing",
        "Real Time Strategy (RTS)", "Role-playing (RPG)", "Simulator",
        "Sport", "Strategy", "Turn-based strategy (TBS)", "Tactical",
        "Hack and slash/Beat 'em up", "Quiz/Trivia"
    )
    private val selectedGenres = mutableListOf<String>()

    // Platform selection
    private val allPlatforms = arrayOf("PC", "PlayStation", "Xbox", "Nintendo Switch", "Mobile")
    private val selectedPlatforms = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // link: https://firebase.google.com/docs/database/android/start
        // website: Firebase Documentation
        // used for: setting up Firebase Authentication and Realtime Database references

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Bind UI
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        switchGenres = findViewById(R.id.switchGenres)
        switchPlatforms = findViewById(R.id.switchPlatforms)
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder)
        btnLogout = findViewById(R.id.btnLogout)

        // Display user info
        val currentUser = auth.currentUser
        if (currentUser != null) {
            tvUserName.text = currentUser.displayName ?: "User"
            tvUserEmail.text = currentUser.email ?: "No email"
            loadSettingsFromFirebase(currentUser.uid)
        } else {
            tvUserName.text = "Guest"
            tvUserEmail.text = ""
        }

        // Show Certain Genres Switch
        switchGenres.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showGenreSelectionDialog()
            currentUser?.let { saveSettingToFirebase(it.uid, "showCertainGenres", isChecked) }
        }

        // Show Specific Platforms Switch
        switchPlatforms.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showPlatformSelectionDialog()
            currentUser?.let { saveSettingToFirebase(it.uid, "showSpecificPlatforms", isChecked) }
        }

        // Spinner listener for default sort order
        spinnerSortOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                currentUser?.let { saveSettingToFirebase(it.uid, "defaultSortOrder", selected) }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        // link: https://developer.android.com/guide/topics/ui/controls/spinner
        // website: Android Developers
        // used for: implementing spinner (dropdown) and handling item selection

        // Logout button
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = android.content.Intent(this, com.example.bmo.activities.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showGenreSelectionDialog() {
        val checkedGenres = BooleanArray(allGenres.size) { selectedGenres.contains(allGenres[it]) }

        AlertDialog.Builder(this)
            .setTitle("Select Genres")
            .setMultiChoiceItems(allGenres, checkedGenres) { _, which, isChecked ->
                if (isChecked) selectedGenres.add(allGenres[which])
                else selectedGenres.remove(allGenres[which])
            }
            .setPositiveButton("Apply") { _, _ ->
                auth.currentUser?.uid?.let { saveSettingToFirebase(it, "selectedGenres", selectedGenres) }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                switchGenres.isChecked = selectedGenres.isNotEmpty() // revert if canceled
            }
            .show()
    }
    // link: https://developer.android.com/develop/ui/views/components/dialogs
    // website: Android Developers
    // used for: creating and handling AlertDialog with multiple choice items

    private fun showPlatformSelectionDialog() {
        val checkedPlatforms = BooleanArray(allPlatforms.size) { selectedPlatforms.contains(allPlatforms[it]) }

        AlertDialog.Builder(this)
            .setTitle("Select Platforms")
            .setMultiChoiceItems(allPlatforms, checkedPlatforms) { _, which, isChecked ->
                if (isChecked) selectedPlatforms.add(allPlatforms[which])
                else selectedPlatforms.remove(allPlatforms[which])
            }
            .setPositiveButton("Apply") { _, _ ->
                auth.currentUser?.uid?.let { saveSettingToFirebase(it, "selectedPlatformsList", selectedPlatforms) }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                switchPlatforms.isChecked = selectedPlatforms.isNotEmpty() // revert if canceled
            }
            .show()
    }

    private fun loadSettingsFromFirebase(uid: String) {
        val userSettingsRef = database.child("users").child(uid).child("settings")
        userSettingsRef.get().addOnSuccessListener { snapshot ->
            switchGenres.isChecked = snapshot.child("showCertainGenres").getValue(Boolean::class.java) ?: true
            switchPlatforms.isChecked = snapshot.child("showSpecificPlatforms").getValue(Boolean::class.java) ?: false

            val genresFromDb = snapshot.child("selectedGenres").children.mapNotNull { it.getValue(String::class.java) }
            selectedGenres.clear()
            selectedGenres.addAll(genresFromDb)

            val platformsFromDb = snapshot.child("selectedPlatformsList").children.mapNotNull { it.getValue(String::class.java) }
            selectedPlatforms.clear()
            selectedPlatforms.addAll(platformsFromDb)

            val defaultSort = snapshot.child("defaultSortOrder").getValue(String::class.java) ?: "Name"
            val sortPosition = resources.getStringArray(R.array.sort_options).indexOf(defaultSort)
            if (sortPosition >= 0) spinnerSortOrder.setSelection(sortPosition)
        }
    }

    private fun saveSettingToFirebase(uid: String, key: String, value: Any) {
        val userSettingsRef = database.child("users").child(uid).child("settings")
        userSettingsRef.child(key).setValue(value)

        // link: https://firebase.google.com/docs/database/android/read-and-write
        // website: Firebase Realtime Database Docs
        // used for: reading data from Firebase Realtime Database and updating UI
    }
}
