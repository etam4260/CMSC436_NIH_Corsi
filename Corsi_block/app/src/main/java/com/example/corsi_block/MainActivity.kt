package com.example.corsi_block

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var login = findViewById<Button>(R.id.launch_login_button)
        var register = findViewById<Button>(R.id.launch_reg_button)

        login.setOnClickListener {
            gotoLogin()
        }

        register.setOnClickListener {
            gotoReg()
        }
        // Set on click listeners to login and register
    }

    fun gotoLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    fun gotoReg() {
        startActivity(Intent(this, RegistrationActivity::class.java))
    }
}