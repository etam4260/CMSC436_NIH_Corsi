package com.example.corsiblocktapping

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var beginTaskBtn: Button
    private lateinit var signInBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var signOutBtn: Button
    private lateinit var tutButton: Button
    private lateinit var loginStatusTV: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // hide default title bar
        this.supportActionBar!!.hide()

        // initialize views by getting references - findViewById()
        initializeViews()

        beginTaskBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlayActivity::class.java))
        }

        signInBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, SignIn::class.java))
        }

        signUpBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, SignUp::class.java))
        }

        tutButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, TutorialActivity::class.java))
        }
        signOutBtn.setOnClickListener {
            auth.signOut()
            onStart()
            Toast.makeText(applicationContext, "You have been signed out",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        displayBtns()
        displayLoginStatus()
    }

    private fun displayLoginStatus() {
        // update login status text view
        // either 'Currently logged in as <email>' or 'Playing as Guest'
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val user = currentUser.email
            Log.i("Current user:", user.toString())
            loginStatusTV.setText("Currently logged in as $user")
        } else {
            Log.i("Current user:", "Guest")
            loginStatusTV.setText("Playing as Guest")
        }

    }

    private fun displayBtns() {
        val currentUser = auth.currentUser
        Log.i("Current user:", auth.currentUser.toString())

        // if not signed in
        if (currentUser != null) {
            // display sign out, not sign in/up
            signInBtn.setVisibility(View.GONE)
            signUpBtn.setVisibility(View.GONE)
            signOutBtn.setVisibility(View.VISIBLE)
        } else {
            signInBtn.setVisibility(View.VISIBLE)
            signUpBtn.setVisibility(View.VISIBLE)
            signOutBtn.setVisibility(View.GONE)
            // display sign in/up, not sign out
        }

    }

    private fun initializeViews() {
        beginTaskBtn = findViewById(R.id.beginTask)
        signInBtn = findViewById(R.id.signIn)
        signUpBtn = findViewById(R.id.signUp)
        signOutBtn = findViewById(R.id.signOut)
        loginStatusTV = findViewById(R.id.loginStatus)
        tutButton = findViewById(R.id.howToPlay)
    }
}