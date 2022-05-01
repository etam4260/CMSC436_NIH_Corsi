package com.example.corsiblocktapping

import android.content.Context
import android.content.Intent
import android.gesture.GestureOverlayView
import android.graphics.*
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.min

class PlayActivity : AppCompatActivity() {
    private lateinit var mFrame: RelativeLayout
    private lateinit var mBitmap: Bitmap
    private lateinit var mHighlightBitmap: Bitmap
    private var mDisplayWidth: Int = 0
    private var mDisplayHeight: Int = 0
    private var mScaledBitmapWidth: Int = 0
    private val rand = Random()
    private var mGestureDetector: GestureDetector? = null

    private var mMoverFuture: ScheduledFuture<*>? = null
    //game state info
    private var mGameState: Int = INITSTATE
    private var mShowingWaitFrames: Int = 0
    private var mSeqLength: Int = 3
    private var mSeqIndex: Int = 0
    private var mSequence: ArrayList<Int> = ArrayList<Int>()
    private var mSeqCorrect: Boolean = true

    // Corsi states we need to keep track of failure per individual
    // trial. You need to repeat it if user fails once, but allow
    // two chance per try.
    private var failures: Int = 0;
    private lateinit var curr_score: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        mFrame = findViewById<View>(R.id.frame) as RelativeLayout
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.whitesquare)
        mHighlightBitmap = BitmapFactory.decodeResource(resources, R.drawable.highlightsquare)


        curr_score = findViewById<TextView>(R.id.score)

        val gestureOverlay = findViewById<View>(R.id.gestures_overlay) as GestureOverlayView
        //gestureOverlay.addOnGesturePerformedListener(this)
        gestureOverlay.setOnTouchListener { v, event ->
            mGestureDetector!!.onTouchEvent(event)
        }
        gestureOverlay.setUncertainGestureColor(Color.TRANSPARENT);
    }

    override fun onResume() {
        super.onResume()
        setupGestureDetector()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            mDisplayWidth = mFrame.width
            mDisplayHeight = mFrame.height
            mScaledBitmapWidth = min(mDisplayWidth, mDisplayHeight) / 6
            Log.i(TAG, "width $mDisplayWidth height $mDisplayHeight")
            removeSquares()
            makeSquares()
            if(mGameState == INITSTATE) {
                mGameState == SHOWING
                start(20)
            }
        }
    }

    private fun makeSquares() {
        for(i in -1..1) {
            for(j in -1..1) {
                val sv = SquareView(applicationContext,
                    mDisplayWidth / 2 + 1.5f * i * mScaledBitmapWidth,
                    mDisplayHeight / 2 + 1.5f * j * mScaledBitmapWidth)
                sv.start()
                mFrame.addView(sv)
                Log.i(TAG, "$i $j added")
            }
        }
    }

    private fun removeSquares() {
        while (mFrame.childCount > 0) {
            (mFrame.getChildAt(0) as SquareView).stop()
        }
    }

    private fun setupGestureDetector() {
        mGestureDetector = GestureDetector(this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                    if(mGameState == TAPPING) {
                        for (i in 0..mFrame.childCount - 1) {
                            val sv = (mFrame.getChildAt(i) as SquareView)
                            if (sv.intersects(event.x, event.y)) {
                                Log.i(TAG, "tap $i")
                                sv.highlightFrames = 2

                                if(i != mSequence[mSeqIndex]) {
                                    mSeqCorrect = false
                                }
                                mSeqIndex++
                                if(mSeqIndex == mSeqLength) {
                                    if(mSeqCorrect) {
                                        mSeqLength++
                                        mGameState = SHOWING

                                        val success_messages = arrayOf("You got it!", "Another one!", "Wow, you are on fire.", "You are a memory monster", "Great job!", "Way to go!")

                                        val rand_ind = (0..6).random()

                                        val toast = Toast.makeText(this@PlayActivity, Html.fromHtml("<font color='#000000' >" + success_messages[rand_ind]+ "</font>"), Toast.LENGTH_LONG)

                                        toast.show()

                                        curr_score.text = mSeqLength.toString()

                                        failures = 0
                                        start(20)
                                    } else {
                                        Log.i(TAG, "you lose :(")

                                        mGameState = SHOWING

                                        failures += 1
                                        if(failures == 2) {
                                            Log.i(TAG, "Go to leaderboard or prompt user to try again or not")

                                            val score =  mSeqLength

                                            startActivity(Intent(this@PlayActivity, Leaderboard::class.java).putExtra("last_score", score))

                                        } else {
                                            // Redo the same number of blocks again...
                                            val toast = Toast.makeText(this@PlayActivity,   Html.fromHtml("<font color='#FF0000' >" + "One more try!"+ "</font>"), Toast.LENGTH_LONG)

                                            toast.show()

                                            Log.i(TAG, mSeqLength.toString())
                                            //Thread.sleep(6000)
                                            //mSeqLength = mSeqLength - 1
                                            start(20)
                                        }
                                    }
                                }
                                break
                            }
                        }
                    }
                    return true
                }

                override fun onDown(event: MotionEvent): Boolean {
                    return true
                }
            })
    }


    private fun start(frames: Int) {
        //Thread.sleep(3000)
        mSeqIndex = 0
        mShowingWaitFrames = frames
        mSequence = ArrayList<Int>()
        mSequence.add(rand.nextInt(9))
        for (i in 1..mSeqLength - 1) {
            val x = rand.nextInt(8) //avoids repeats
            if(x == mSequence.last()) {
                mSequence.add(8)
            } else {
                mSequence.add(x)
            }
        }
        Log.i(TAG, mSeqLength.toString())

        val executor = Executors
            .newScheduledThreadPool(1)
        mMoverFuture = executor.scheduleWithFixedDelay({
            if(mSeqIndex == mSeqLength) {
                mGameState = TAPPING
                mSeqIndex = 0
                mSeqCorrect = true
                this.stop()
            } else {
                mShowingWaitFrames--
                if(mShowingWaitFrames == 8) {
                    (mFrame.getChildAt(mSequence[mSeqIndex]) as SquareView).highlightFrames = 8
                } else if(mShowingWaitFrames == 0) {
                    mShowingWaitFrames = 10
                    mSeqIndex += 1
                }
            }
        }, 0, REFRESH_RATE.toLong(), TimeUnit.MILLISECONDS)
    }

    private fun stop() {
        if (null != mMoverFuture) {
            if (!mMoverFuture!!.isDone) {
                mMoverFuture!!.cancel(true)
            }
        }
    }

    inner class SquareView internal constructor(context: Context, x: Float, y: Float) :
        View(context) {
        private val mPainter = Paint()
        private var mMoverFuture: ScheduledFuture<*>? = null
        private var mScaledBitmap: Bitmap? = null
        private var mScaledHighlightBitmap: Bitmap? = null

        private var mXPos: Float = 0.toFloat()
        private var mYPos: Float = 0.toFloat()
        private val mRadius: Float
        var highlightFrames: Int = 0

        init {
            Log.i(TAG, "Creating Square at: x:$x y:$y")
            createScaledBitmap()
            mRadius = (mScaledBitmapWidth / 2).toFloat()
            mXPos = x - mRadius
            mYPos = y - mRadius
            mPainter.isAntiAlias = true
        }

        private fun createScaledBitmap() {
            this.mScaledBitmap = Bitmap.createScaledBitmap(
                mBitmap,
                mScaledBitmapWidth, mScaledBitmapWidth, false
            )
            this.mScaledHighlightBitmap = Bitmap.createScaledBitmap(
                mHighlightBitmap,
                mScaledBitmapWidth, mScaledBitmapWidth, false
            )
        }

        fun start() {
            val executor = Executors
                .newScheduledThreadPool(1)
            mMoverFuture = executor.scheduleWithFixedDelay({
                this.postInvalidate()
            }, 0, REFRESH_RATE.toLong(), TimeUnit.MILLISECONDS)
        }

        @Synchronized
        fun intersects(x: Float, y: Float): Boolean {
            val xDist = x - (mXPos + mRadius)
            val yDist = y - (mYPos + mRadius)
            return abs(xDist) <= mRadius && abs(yDist) <= mRadius
        }

        internal fun stop() {
            if (null != mMoverFuture) {
                if (!mMoverFuture!!.isDone) {
                    mMoverFuture!!.cancel(true)
                }
                mFrame.post {
                    mFrame.removeView(this)
                    Log.i(TAG, "Square removed from view!")
                }
            }
        }

        @Synchronized
        override fun onDraw(canvas: Canvas) {
            canvas.save()
            if(highlightFrames > 0) {
                canvas.drawBitmap(mScaledHighlightBitmap!!, mXPos, mYPos, mPainter)
                highlightFrames--
                //Log.i(TAG, "hl-- $highlightFrames")
            } else {
                canvas.drawBitmap(mScaledBitmap!!, mXPos, mYPos, mPainter)
                //Log.i(TAG, "hl $highlightFrames")
            }
            canvas.restore()
        }
    }

    companion object {
        private const val REFRESH_RATE = 100 //ms, not fps
        private const val INITSTATE = 0 //game state: has not started
        private const val SHOWING = 1 //game state: random blocks are flashing
        private const val TAPPING = 2 //game state: player is tapping them back in order
        private const val TAG = "Corsi-PlayScreen"
    }
}