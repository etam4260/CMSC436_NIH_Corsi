package com.example.corsiblocktapping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock.sleep
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class Leaderboard : AppCompatActivity() {

    companion object {
        private const val TAG = "LeaderboardLog"
    }

    private var DB = FirebaseRealTimeDB()
    private lateinit var auth: FirebaseAuth

    // m1-10 will be stored in array for easy iterating
    private lateinit var mLeaderboard: Array<TextView>
    private lateinit var m1TV: TextView
    private lateinit var m2TV: TextView
    private lateinit var m3TV: TextView
    private lateinit var m4TV: TextView
    private lateinit var m5TV: TextView
    private lateinit var m6TV: TextView
    private lateinit var m7TV: TextView
    private lateinit var m8TV: TextView
    private lateinit var m9TV: TextView
    private lateinit var m10TV: TextView

    private lateinit var last_scoreTV: TextView
    private lateinit var high_scoreTV: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        Log.i(TAG, "onCreate()")

        initializeViews()

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

        // set high score
        if (currentUser != null && currentUser.email != null) {
            DB.setHighScore(currentUser.email!!, high_scoreTV)
        } else {
            high_scoreTV.setText("make an account to track you high score")
        }

        // fetch and populate leaderboard
        DB.getLeaderboard(mLeaderboard)
    }

// unnecessary, might remove later
//    override fun onStart() {
//        super.onStart()
//
//        // todo add event listener for updates to leaderboard to update UI
////        databaseAuthors.addValueEventListener(object : ValueEventListener {
////            override fun onDataChange(dataSnapshot: DataSnapshot) {
//    }

    private fun initializeViews() {
        high_scoreTV = findViewById(R.id.highest_score)
        last_scoreTV = findViewById(R.id.last_score)
        m1TV = findViewById(R.id.top1)
        Log.i(TAG, "m1tv has id ${m1TV.id}")
        m2TV = findViewById(R.id.top2)
        Log.i(TAG, "m2tv has id ${m2TV.id}")
        m3TV = findViewById(R.id.top3)
        m4TV = findViewById(R.id.top4)
        m5TV = findViewById(R.id.top5)
        m6TV = findViewById(R.id.top6)
        m7TV = findViewById(R.id.top7)
        m8TV = findViewById(R.id.top8)
        m9TV = findViewById(R.id.top9)
        m10TV = findViewById(R.id.top10)
        Log.i(TAG, "m10tv has id ${m10TV.id}")
        mLeaderboard = arrayOf(m1TV, m2TV, m3TV, m4TV, m5TV, m6TV, m7TV, m8TV, m9TV, m10TV)
        Log.i(TAG, "mLeaderboard[0] ${mLeaderboard[0].id}")
        Log.i(TAG, "mLeaderboard[1] ${mLeaderboard[1].id}")
    }


    private class FirebaseRealTimeDB {

        companion object {
            private const val TAG = "FirebaseRealTimeDB"
        }
        val database = Firebase.database

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

                                    // get referece to Score to be updated
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

                        // if no match found, create new highscore
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

        // returns high score associated with email arg, or -1 if not found
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

        // todo make this update UI
        fun getLeaderboard(leaderboard: Array<TextView>) {
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
                    Log.i(TAG, "There were ${num_scores} scores")
                    val sorted_scores = scores.sortedByDescending { it.score }
                    Log.i(TAG, "Here are stored scores: ${sorted_scores.toString()}" )

                    // set leaderboard TextView(s)
                    var view_index: Int = 0
                    for (view in leaderboard) {
                        val cur_email: String = sorted_scores.getOrNull(view_index)?.email.toString()
                        val cur_score: String = sorted_scores.getOrNull(view_index)?.score.toString()
                        view.setText("${view_index+1}. ${cur_email} ${cur_score}")
                        view_index ++
                    }
                    Log.i(TAG, "Updated ${view_index} leaderboard views, should be ${num_scores}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, " onCancelled was called")
                }
            })

        }
    }
}