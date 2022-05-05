package com.example.corsiblocktapping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class Leaderboard : AppCompatActivity() {

    companion object {
        private const val TAG = "LeaderboardLog"
    }

    private var DB = FirebaseRealTimeDB()
    private lateinit var auth: FirebaseAuth

    // leaderboard rows m1-10 will be stored in arrays for easy iterating
    private lateinit var mLeaderboard: Array<TextView>
    private lateinit var mEmail: Array<TextView>
    private lateinit var mScore: Array<TextView>

    private lateinit var m1Rank: TextView
    private lateinit var m2Rank: TextView
    private lateinit var m3Rank: TextView
    private lateinit var m4Rank: TextView
    private lateinit var m5Rank: TextView
    private lateinit var m6Rank: TextView
    private lateinit var m7Rank: TextView
    private lateinit var m8Rank: TextView
    private lateinit var m9Rank: TextView
    private lateinit var m10Rank: TextView

    private lateinit var m1Score: TextView
    private lateinit var m2Score: TextView
    private lateinit var m3Score: TextView
    private lateinit var m4Score: TextView
    private lateinit var m5Score: TextView
    private lateinit var m6Score: TextView
    private lateinit var m7Score: TextView
    private lateinit var m8Score: TextView
    private lateinit var m9Score: TextView
    private lateinit var m10Score: TextView

    private lateinit var m1Email: TextView
    private lateinit var m2Email: TextView
    private lateinit var m3Email: TextView
    private lateinit var m4Email: TextView
    private lateinit var m5Email: TextView
    private lateinit var m6Email: TextView
    private lateinit var m7Email: TextView
    private lateinit var m8Email: TextView
    private lateinit var m9Email: TextView
    private lateinit var m10Email: TextView

    private lateinit var tryAgain: Button
    private lateinit var startScreen: Button

    private lateinit var last_scoreTV: TextView
    private lateinit var high_scoreTV: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        Log.i(TAG, "onCreate()")

        initializeViews()

        tryAgain.setOnClickListener {
            startActivity(Intent(this, PlayActivity::class.java))
        }

        startScreen.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        // Initialize Firebase Auth
        auth = Firebase.auth

        // get email for current user
        val email: String
        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.email != null)
                email = currentUser.email.toString()
            else
                email = "Guest" // in case email field is null
        } else {
            email = "Guest"
        }
        Log.i(TAG, "email is $email")

        // get score from most recent game
        val mIntent: Intent = getIntent()
        val last_score = mIntent.getIntExtra("last_score", -1)
        Log.i(TAG, "score is $last_score")

        // save score to scores and high scores in Firebase Real Time DB
        if (last_score >= 0 && email != "Guest") {
            DB.saveScore(email, last_score)
            DB.updateHighScore(email, last_score)
        }

        // hide default title bar
        this.supportActionBar!!.hide()

        // set last score
        last_scoreTV.setText("${last_score}")

        // set high score text view
        if (currentUser != null && currentUser.email != null) {
            DB.setHighScore(currentUser.email!!, high_scoreTV)
        } else {
            high_scoreTV.setText("Make an account to start tracking your high score!")
        }

        // fetch and populate leaderboard
        DB.getLeaderboard(mLeaderboard, mEmail, mScore)
    }

    private fun initializeViews() {
        high_scoreTV = findViewById(R.id.highest_score)
        last_scoreTV = findViewById(R.id.last_score)

        m1Rank = findViewById(R.id.top1_rank)
        m2Rank = findViewById(R.id.top2_rank)
        m3Rank = findViewById(R.id.top3_rank)
        m4Rank = findViewById(R.id.top4_rank)
        m5Rank = findViewById(R.id.top5_rank)
        m6Rank = findViewById(R.id.top6_rank)
        m7Rank = findViewById(R.id.top7_rank)
        m8Rank = findViewById(R.id.top8_rank)
        m9Rank = findViewById(R.id.top9_rank)
        m10Rank = findViewById(R.id.top10_rank)

        m1Email = findViewById(R.id.top1_email)
        m2Email = findViewById(R.id.top2_email)
        m3Email = findViewById(R.id.top3_email)
        m4Email = findViewById(R.id.top4_email)
        m5Email = findViewById(R.id.top5_email)
        m6Email = findViewById(R.id.top6_email)
        m7Email = findViewById(R.id.top7_email)
        m8Email = findViewById(R.id.top8_email)
        m9Email = findViewById(R.id.top9_email)
        m10Email = findViewById(R.id.top10_email)

        m1Score = findViewById(R.id.top1_score)
        m2Score = findViewById(R.id.top2_score)
        m3Score = findViewById(R.id.top3_score)
        m4Score = findViewById(R.id.top4_score)
        m5Score = findViewById(R.id.top5_score)
        m6Score = findViewById(R.id.top6_score)
        m7Score = findViewById(R.id.top7_score)
        m8Score = findViewById(R.id.top8_score)
        m9Score = findViewById(R.id.top9_score)
        m10Score = findViewById(R.id.top10_score)

        tryAgain = findViewById<Button>(R.id.tryAgain)
        startScreen = findViewById<Button>(R.id.startScreen)

        mLeaderboard = arrayOf(m1Rank, m2Rank, m3Rank, m4Rank, m5Rank, m6Rank, m7Rank, m8Rank, m9Rank, m10Rank)
        mEmail = arrayOf(m1Email, m2Email, m3Email, m4Email, m5Email, m6Email, m7Email, m8Email, m9Email, m10Email)
        mScore = arrayOf(m1Score, m2Score, m3Score, m4Score, m5Score, m6Score, m7Score, m8Score, m9Score, m10Score)
    }

    private class FirebaseRealTimeDB {

        companion object {
            private const val TAG = "FirebaseRealTimeDB"
        }

        // store a list of all scores in Firebase database
        private lateinit var scores: MutableList<Score>

        //database reference object
        private lateinit var databaseScores: DatabaseReference
        private lateinit var databaseHighScores: DatabaseReference

        // save score list of all scores in Firebase DB
        fun saveScore(email: String, score: Int) {
            Log.i(TAG, "saveScore w email $email and score $score")
            // get reference of scores node in db
            databaseScores = FirebaseDatabase.getInstance().getReference("scores")

            if (!TextUtils.isEmpty(email)) {
                // get unique id to use as Primary key
                val id = databaseScores.push().key

                // create a Score object
                val scoreObj = Score(id!!, email, score)

                // save score to db
                databaseScores.child(id).setValue(scoreObj)
            }
        }

        // replace old high score if arg is greater
        fun updateHighScore(email: String, score: Int) {
            Log.i(TAG, "updateHighScore w email $email and score $score")
            // get reference of high_scores node in db
            databaseHighScores = FirebaseDatabase.getInstance().getReference("high_scores")

            if (!TextUtils.isEmpty(email)) {
                // use SingleValueEvent to run once, not every time database is updated
                databaseHighScores.addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (scoreSnapshot in snapshot.children) {
                            val curScore = scoreSnapshot.getValue<Score>()
                            if (curScore!!.email == email) {    // email matches
                                if (curScore!!.score < score) { // new score is greater

                                    // get reference to Score to be updated
                                    val old_score = FirebaseDatabase.getInstance()
                                        .getReference("high_scores").child(curScore.scoreId)

                                    // build new Score object
                                    val new_score = Score(curScore.scoreId, email, score)

                                    // replace old Score
                                    old_score.setValue(new_score)
                                }
                                return
                            }
                        }

                        // if no match found, create new high score
                        var id = databaseHighScores.push().key.toString()
                        val high_score = Score(id, email, score)
                        databaseHighScores.child(id).setValue(high_score)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "databaseHighScores onCancelled: error getting high scores")
                    }
                })
            }
        }

        // fetch current user's high score from Firebase RTDB and write it to high_scoreTV
        fun setHighScore(email : String, high_scoreTV: TextView) {
            var high_score: Int = -1
            var lock = Object()
            Log.i(TAG, "setHighScore for email ${email}")

            databaseHighScores = FirebaseDatabase.getInstance().getReference("high_scores")
            // use SingleValueEvent to run once, not every time database is updated
            databaseHighScores.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (scoreSnapshot in snapshot.children) {
                        val curScore = scoreSnapshot.getValue<Score>()
                        if (curScore!!.email == email) {
                            //Log.i(TAG, "found match for email ${curScore!!.email}")
                            high_score = curScore!!.score
                            //Log.i(TAG, "found high score ${high_score}")
                            high_scoreTV.setText("${high_score}")
                            return
                        }
                    }

                    //high_scoreTV.setText("no scores found")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, "onCancelled error")
                }
            })
        }

        fun getLeaderboard(leaderboard: Array<TextView>, email: Array<TextView>, score: Array<TextView>) {
            // list to store scores
            scores = ArrayList()

            // get reference of scores node in db
            databaseScores = FirebaseDatabase.getInstance().getReference("scores")

            // run on start, then whenever leaderboard is updated
            databaseScores.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    var num_scores = 0

                    // instead of storing and sorting all scores we could only keep track of the
                    // top 10 encountered, and compare the smallest one
                    // optional todo if efficiency is a problem, optimize this
                    for (scoreSnapshot in snapshot.children) {
                        num_scores ++
                        val cur_score = scoreSnapshot.getValue<Score>()
                        //Log.i("FirebaseRealtimeDB", "Got email ${cur_score!!.email} score ${cur_score!!.score}")
                        scores!!.add(cur_score!!)
                    }
                    Log.i(TAG, "There were ${num_scores} Scores")
                    val sorted_scores = scores.sortedByDescending { it.score }
                    Log.i(TAG, "Here are stored Scores: ${sorted_scores.toString()}" )

                    // set leaderboard TextView(s)
                    var view_index: Int = 0

                    while (view_index < 10) {
                        val curScore: Score? = sorted_scores.getOrNull(view_index)

                        // set rank view
                        leaderboard[view_index].setText("${view_index + 1}.")

                        // set email view
                        var cur_email: String = ""
                        if (curScore != null)
                            cur_email = curScore.email
                        email[view_index].setText(cur_email)

                        // set score view
                        var cur_score: String = ""
                        if (curScore != null)
                            cur_score = curScore.score.toString()
                        score[view_index].setText(cur_score)

                        view_index++
                        //Log.i(TAG, "Rank: ${view_index}")
                        //Log.i(TAG, "Email: $cur_email" )
                        //Log.i(TAG, "Scores: $cur_score" )
                    }

                    Log.i(TAG, "Updated ${view_index} rows of leaderboard")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, " onCancelled was called")
                }
            })

        }
    }
}