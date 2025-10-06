package com.example.bmo.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        // Link: https://developers.google.com/identity/sign-in/android/start-integrating
        // Website: Google Developers
        // Used for setting up Google Sign-In with requestIdToken and email
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)

        btnLogin.setOnClickListener { loginWithEmail() }
        btnGoogleLogin.setOnClickListener { signInWithGoogle() }
    }

    private fun loginWithEmail() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show()
            return
        }
        // Link: https://firebase.google.com/docs/auth/android/password-auth
        // Website: Firebase Docs
        // Used for implementing email/password authentication with FirebaseAuth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Welcome ${auth.currentUser?.displayName ?: "User"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMain()
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun signInWithGoogle() {
        // Force Google account selector
        // Link: https://developers.google.com/identity/sign-in/android/sign-in
        // Website: Google Developers
        // Used for triggering Google account selection for sign-in
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

    // Link: https://firebase.google.com/docs/auth/android/google-signin
    // Website: Firebase Docs
    // Used for authenticating Google account with Firebase using idToken
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
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
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

        // Only update fields without overwriting existing children (like library/favourites/settings)
        database.updateChildren(updates)
    }

    private fun navigateToMain() {
        val intent = Intent(this, com.example.bmo.MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
