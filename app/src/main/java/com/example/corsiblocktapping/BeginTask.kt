package com.example.corsiblocktapping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class BeginTask : AppCompatActivity() {

    private lateinit var backBtn: Button
    private lateinit var leaderboardBtn: Button
    private var score: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_begin_task)

        // hide default title bar
        this.supportActionBar!!.hide()

        backBtn = findViewById(R.id.backButton)
        backBtn.setOnClickListener {
            // return to main activity
            onBackPressed()
            //startActivity(Intent(this@BeginTask, MainActivity::class.java))
        }

        leaderboardBtn = findViewById(R.id.leaderboardButton)
        leaderboardBtn.setOnClickListener {
            // start Leaderboard Activity, passing in current user's score
            val myIntent: Intent = Intent(this@BeginTask, Leaderboard::class.java)
            myIntent.putExtra("last_score", score)
            startActivity(myIntent)
        }
    }
}