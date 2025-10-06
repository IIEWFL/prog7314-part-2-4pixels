package com.example.bmo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bmo.R
import com.example.bmo.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvLoginInstead: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // Link: https://developers.google.com/identity/sign-in/android/start-integrating
        // Website: Google Developers
        // Used for configuring Google Sign-In (requestIdToken & requestEmail)
        // Google Sign-in config

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etRegisterPassword)
        tvLoginInstead = findViewById(R.id.tvLoginInstead)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGoogleRegister = findViewById<LinearLayout>(R.id.btnGoogleRegister)

        btnRegister.setOnClickListener { registerWithEmail() }
        btnGoogleRegister.setOnClickListener { signInWithGoogle() }

        tvLoginInstead.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerWithEmail() {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (name.isBlank() || surname.isBlank() || email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }
        // Link: https://firebase.google.com/docs/auth/android/start
        // Website: Firebase Docs
        // Used for creating a new user account with email & password in Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    val fullName = "$name $surname"

                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    user.updateProfile(profileUpdates)
                        .addOnCompleteListener {
                            Toast.makeText(this, "Registered! Welcome $fullName", Toast.LENGTH_SHORT).show()
                            // Auto-login & redirect to Home
                            startActivity(Intent(this, com.example.bmo.MainActivity::class.java))
                            finish()
                        }

                    saveUserToDatabase(user.uid, name, surname, email)
                    user.sendEmailVerification()
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(uid: String, name: String, surname: String, email: String) {
        val database = FirebaseDatabase.getInstance().getReference("users").child(uid)
        val updates = mapOf(
            "uid" to uid,
            "name" to name,
            "surname" to surname,
            "email" to email
        )
        database.updateChildren(updates)
    }
    // Link: https://firebase.google.com/docs/database/android/read-and-write
    // Website: Firebase Docs
    // Used for writing user information into Firebase Realtime Database
    private fun signInWithGoogle() {
        // Force Google account selector
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    @Deprecated("Use registerForActivityResult in new code")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Link: https://developers.google.com/identity/sign-in/android/sign-in
    // Website: Google Developers
    // Used for triggering Google account selector when registering with Google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    val displayName = user.displayName ?: "User"
                    val email = user.email ?: ""
                    saveUserToDatabase(user.uid, displayName, "", email)
                    Toast.makeText(this, "Welcome $displayName", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, com.example.bmo.MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    // Link: https://firebase.google.com/docs/auth/android/google-signin
    // Website: Firebase Docs
    // Used for authenticating Google account with Firebase using GoogleAuthProvider
    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
