package com.example.corsiblocktapping

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class TutorialActivity : AppCompatActivity() {

    private lateinit var top_descriptions: Array<String>
    private lateinit var bottom_descriptions: Array<String>
    private lateinit var images: ImageView
    private lateinit var bottom_desc_view: TextView
    private lateinit var top_desc_view: TextView
    private lateinit var next: TextView
    private lateinit var prev: TextView
    private var current_screen: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        top_descriptions = arrayOf("Thanks for checking out The Corsi Block Tap. I\\'m sure you will enjoy your stay here and learn a little bit more about this commonly known psychology experiment.",
            "When you first click play you will be presented with 9 white boxes. One of the 9 boxes will get highlighted yellow at a time with a total of boxes highlighted equal to the number presented at the bottom of the screen. The game starts at 3.",
            "An example of a highlighted box is shown above. Make sure you catch a glimpse as they go by fast.",
            "If you fail twice for a given round, you will be sent to the leaderboard to see your most recent score, your high score, as well a global leaderboard.",
            ""
            )

        bottom_descriptions = arrayOf("The Corsi-Block Tapping task is a visual-spatial memory assessment. In this game, you must memorize a series of blocks that have been tapped by the computer and then you must tap the blocks in the same order.",
            "Once the computer is done highlighting these blocks, you must click on the same boxes in the same order as the computer.",
            "Fail to heed the computers commands and you will be given a second chance. You get a total of one failure per round. The second failure will end the game.",
            "Hit play again if you would like another go at it. Otherwise, hit the home button to get back to the snazzy home screen.",
            "Okay! It looks like you know all the basics. Hit next to get back to the home screen and then tap on begin task to start."
            )

        next = findViewById<Button>(R.id.next)
        prev = findViewById<Button>(R.id.prev)

        images = findViewById(R.id.tut_image)
        bottom_desc_view = findViewById(R.id.description)
        top_desc_view = findViewById(R.id.intro_descrip)

        current_screen = 0

        next.setOnClickListener {

            current_screen = current_screen + 1
            Log.i("Next desc", current_screen.toString())
            this.nextDescription(current_screen)

        }

        prev.setOnClickListener {
            current_screen = current_screen - 1
            this.nextDescription(current_screen)
        }
    }

    private fun nextDescription(current: Int) {
        Log.i("Next desc", "Next desc")
        Log.i("Next desc", current.toString())
        if (current < 0) {
            // Just go back to main page
            current_screen = 0
            startActivity(Intent(this, MainActivity::class.java))

        } else if(current == 5) {
            current_screen = 0
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            top_desc_view.text = top_descriptions[current]
            bottom_desc_view.text = bottom_descriptions[current]
            if(current == 0) {
                images.setImageResource(R.drawable.block3)
            } else if(current == 1) {
                images.setImageResource(R.drawable.game_demo)
            } else if(current == 2) {
                images.setImageResource(R.drawable.highlightsquare)

            } else if(current == 3) {
                images.setImageResource(R.drawable.leadboard_demo)
            } else if(current == 4) {
                images.setImageResource(R.drawable.block3)
            }
        }
    }

}