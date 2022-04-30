package com.example.corsiblocktapping

import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

//https://firebase.google.com/docs/database/android/read-and-write?authuser=0#kotlin+ktx_1
class FirebaseRealTimeDB {

    val database = Firebase.database
    // store a list of all scores in Firebase database
    private lateinit var scores: MutableList<Score>

    //database reference object
    private lateinit var databaseScores: DatabaseReference

    // attempt to save score to Firebase DB, returning true if successful
    fun saveScore(email: String, score: Int) : Boolean {

        // get reference of scores node in db
        databaseScores = FirebaseDatabase.getInstance().getReference("scores")

        if (!TextUtils.isEmpty(email)) {
            // get unique id to use as Primary key
            val id = databaseScores.push().key

            // create a Score object
            val scoreObj = Score(id!!, email, score)

            // save score to db
            databaseScores.child(id).setValue(scoreObj)

            // return true for sucess
            return true;
        } else {
            // return false for failure
            return false
        }


        // add entry to DB
    }

    // todo this and change return type next line
    fun getLeaderboard() : String {
        // list to store scores
        scores = ArrayList()

        // get reference of scores node in db
        databaseScores = FirebaseDatabase.getInstance().getReference("scores")

        // run on start, then whenever leaderboard is updated
        databaseScores.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                var num_scores = 0
                for (scoreSnapshot in snapshot.children) {
                    num_scores ++
                    val cur_score = scoreSnapshot.getValue<Score>()
                    Log.i("FirebaseRealtimeDB", "Got email ${cur_score!!.email} score ${cur_score!!.score}")
                    scores.add(cur_score)
                }
                Log.i("FirebaseRealtimeDB", "There were ${num_scores} scores")
                Log.i("FirebaseRealtimeDB", "Here are stored scores: ${scores.toString()}" )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("FirebaseRealtimeDB", " onCancelled was called")
            }
        })

//        databaseScores.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot item_snapshot:dataSnapshot.getChildren()) {
//
//                Log.d("item id ",item_snapshot.child("item_id").getValue().toString());
//                Log.d("item desc",item_snapshot.child("item_desc").getValue().toString());
//            }
//            }
//        }


        val TAG = "FirebaseRTDB"
        Log.i(TAG, databaseScores.toString())


        return "temp return value"
    }
}