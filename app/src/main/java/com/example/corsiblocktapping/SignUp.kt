package com.example.corsiblocktapping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {

    private lateinit var emailTV: TextView
    private lateinit var passwordTV: TextView
    private lateinit var confirmPasswordTV: TextView
    private lateinit var signUpBtn: Button
    private lateinit var errorTV: TextView
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        // hide default title bar
        this.supportActionBar!!.hide()

        initializeViews()

        signUpBtn.setOnClickListener {
            registerNewUser()

            //startActivity(Intent(this@SignUp, MainActivity::class.java))
        }

    }

    override fun onStart() {
        super.onStart()

        // hide errorTV until we have a message to display
        errorTV.setVisibility(View.GONE)
    }

    private fun registerNewUser() {
        // get user input
        val email: String = emailTV.text.toString()
        val password: String = passwordTV.text.toString()
        val confirmPassword: String = confirmPasswordTV.text.toString()

        Log.i("Email:", email)
        Log.i("Password:", password)
        Log.i("Confirm:", confirmPassword)

        // validate inputs
        val emailRegex = Regex("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'" +
                "*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x" +
                "5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z" +
                "0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4" +
                "][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z" +
                "0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|" +
                "\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])")

        val validEmail: Boolean = emailRegex.matches(email)
        val validInputs: Boolean = !email.isNullOrEmpty() && !password.isNullOrEmpty()
                && !confirmPassword.isNullOrEmpty() && (password == confirmPassword)
                && validEmail && password.length >= 6

        Log.i("Valid Email?", validEmail.toString())
        Log.i("Valid Inputs?", validInputs.toString())

        if (validInputs) {
            // todo register user via Firebase
            val register = mAuth!!.createUserWithEmailAndPassword(email, password)

            register.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // toast confirmation
                    Toast.makeText(applicationContext, "Account created successfully", Toast.LENGTH_LONG).show()

                    Log.i("Valid Email?", task.isSuccessful.toString())
                    // return to main menu
                    onBackPressed()
                } else {
                    Log.i("Valid Email?", task.isSuccessful.toString())
                    errorTV.setVisibility(View.VISIBLE)
                    errorTV.setText("Failed to create account")
                }
            }

        } else {
            // display message in error textview
            errorTV.setVisibility(View.VISIBLE)
            var errorText: String = ""
            if (!validEmail)
                errorText = "Enter a valid email"
            if (password != confirmPassword)
                errorText = "Passwords must match"
            if (password.length < 6)
                errorText = "Password must be at least 6 characters long"
            if (confirmPassword.isNullOrEmpty())
                errorText = "Confirm your password"
            if (password.isNullOrEmpty())
                errorText = "Enter a password"
            if (email.isNullOrEmpty())
                errorText = "Enter a email"

            errorTV.setText(errorText)
        }


        // todo toast success or not
    }

    private fun initializeViews() {
        emailTV = findViewById(R.id.email)
        passwordTV = findViewById(R.id.password)
        confirmPasswordTV = findViewById(R.id.confirm_password)
        signUpBtn = findViewById(R.id.signUp)
        errorTV = findViewById(R.id.error)
    }
}