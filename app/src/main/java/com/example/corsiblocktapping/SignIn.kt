package com.example.corsiblocktapping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SignIn : AppCompatActivity() {

    private lateinit var emailTV: TextView
    private lateinit var passwordTV: TextView
    private lateinit var signInBtn: TextView
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()

        // hide default title bar
        this.supportActionBar!!.hide()

        // initialize views by getting references - findViewById()
        initializeViews()

        signInBtn.setOnClickListener { login() }

    }

    override fun onStart() {
        super.onStart()
        // if isSignedIn already, return to MainActivity
        if (mAuth!!.currentUser != null) {
            onBackPressed()
        }
    }

    private fun login() {
        // get user input
        val email: String = emailTV.text.toString()
        val password: String = passwordTV.text.toString()

        // auth user via Firebase
        val callback = mAuth!!.signInWithEmailAndPassword(email, password)
        callback.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // toast 'logged in as <email>'
                val email2: String = mAuth!!.currentUser!!.email.toString()
                Toast.makeText(applicationContext, "Logged in as $email2", Toast.LENGTH_LONG).show()
                // return to main menu
                onBackPressed()
            } else {
                // toast failure
                Toast.makeText(applicationContext, "Invalid credentials, try again",
                    Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun initializeViews() {
        emailTV = findViewById(R.id.email)
        passwordTV = findViewById(R.id.password)
        signInBtn = findViewById(R.id.signIn)
    }
}