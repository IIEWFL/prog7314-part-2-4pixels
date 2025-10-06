package com.example.bmo

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.bmo.activities.LoginActivity
import com.example.bmo.activities.RegisterActivity
import com.example.bmo.activities.SettingsActivity
import com.example.bmo.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * MainActivity is the central activity of the BMO app.
 * It handles the navigation drawer, bottom navigation, and fragment management.
 *
 * References:
 * 1. Firebase Authentication: https://firebase.google.com/docs/auth
 * 2. DrawerLayout & NavigationView: https://developer.android.com/guide/navigation/navigation-ui
 * 3. BottomNavigationView: https://developer.android.com/reference/com/google/android/material/bottomnavigation/BottomNavigationView
 * 4. Fragment Transactions: https://developer.android.com/guide/fragments/fragmentmanager
 */
class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnMenu: ImageButton
    private lateinit var bmoDrawerFace: ImageView
    private lateinit var drawerGreeting: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.endDrawer)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        btnMenu = findViewById(R.id.btnMenu)

        // Access header views
        val headerView = navigationView.getHeaderView(0)
        bmoDrawerFace = headerView.findViewById(R.id.bmoDrawerFace)
        drawerGreeting = headerView.findViewById(R.id.drawerGreeting)

        val currentUser = auth.currentUser
        updateDrawerForUser(currentUser)
        updateBottomNavForUser(currentUser)

        // Menu button toggles drawer
        btnMenu.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.openDrawer(navigationView)
            } else {
                drawerLayout.closeDrawer(navigationView)
            }
        }

        // Bottom nav switching
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_search -> loadFragment(SearchFragment())
                R.id.nav_library -> {
                    if (currentUser != null) {
                        loadFragment(LibraryFragment())
                    }
                }
            }
            true
        }

        // Drawer menu handling
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_sign_in -> startActivity(Intent(this, LoginActivity::class.java))
                R.id.nav_register -> startActivity(Intent(this, RegisterActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_logout -> logoutUser()
            }
            drawerLayout.closeDrawer(navigationView)
            true
        }

        // Default fragment
        loadFragment(HomeFragment())
    }

    /**
     * Update drawer UI based on the current Firebase user.
     * @param user - currently logged-in FirebaseUser or null if not logged in
     */

    private fun updateDrawerForUser(user: FirebaseUser?) {
        val signInItem = navigationView.menu.findItem(R.id.nav_sign_in)
        val registerItem = navigationView.menu.findItem(R.id.nav_register)
        val logoutItem = navigationView.menu.findItem(R.id.nav_logout)

        if (user != null) {
            // User is logged in
            drawerGreeting.text = "Hello, ${user.displayName ?: "Player"}"
            signInItem.isVisible = false
            registerItem.isVisible = false
            logoutItem.isVisible = true
        } else {
            // No user logged in
            drawerGreeting.text = "Hello Gamer!"
            signInItem.isVisible = true
            registerItem.isVisible = true
            logoutItem.isVisible = false
        }
    }
    /**
     * Update bottom navigation based on user authentication.
     * Hides library tab if no user is logged in.
     */
    private fun updateBottomNavForUser(user: FirebaseUser?) {
        val libraryItem = bottomNavigationView.menu.findItem(R.id.nav_library)
        libraryItem.isVisible = user != null
    }

    private fun logoutUser() {
        auth.signOut()
        updateDrawerForUser(null)
        updateBottomNavForUser(null)
        loadFragment(HomeFragment())
    }
    /**
     * Logs out the user and resets the UI.
     */

    override fun onResume() {
        super.onResume()
        val currentUser = auth.currentUser
        updateDrawerForUser(currentUser)
        updateBottomNavForUser(currentUser)
    }

    /**
     * Replace current fragment with the given fragment.
     * @param fragment - Fragment to display
     */

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
