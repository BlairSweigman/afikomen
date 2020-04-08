/*
 * Copyright (c) 2020. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Blair Sweigman <blair.sweigman@gmail.com>
 */

package ca.worthconsulting.afikomen

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

/**
 * The Main Class - pretty much does everything
 */
class MainActivity : AppCompatActivity() {
    private val boxes = mutableListOf<ImageView>()
    private var afikomen: Int? = null
    private lateinit var resetButton: Button
    private lateinit var txtHeader: TextView
    private lateinit var gameGrid: GridLayout
    private lateinit var imgAfikomen: ImageView
    private var startTime: Long = 0L
    private lateinit var mAdView: AdView
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Creates the game board and initializes everything
     *
     * @param savedInstanceState {Bundle} instance state, currently protecting the Afikomen
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        savedInstanceState?.let {
            afikomen = it.getInt("AFIKOMEN", 0)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtHeader = findViewById(R.id.txtHeader)
        resetButton = findViewById(R.id.reset_button)
        gameGrid = findViewById(R.id.game_grid)
        imgAfikomen = findViewById(R.id.afikomen)
        resetButton.setOnClickListener(ResetListener())
        afikomen = afikomen ?: Random.nextInt(0, 24)
        for (i in 0..24) {
            makeBox(i)
        }

        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    /**
     * Add a box to the box list with a listener to receive click
     * @param boxNumber Int id number of box to guess against afikomen
     */
    private fun makeBox(boxNumber: Int) {
        val temp = resources.getIdentifier("box" + (boxNumber + 1).toString(), "id", packageName)
        val box = findViewById<ImageView>(temp)
        box.setOnClickListener(this.SearchListener(boxNumber))
        boxes.add(box)
    }


    /**protect the afikomen if activity needs to be recreated
     *
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("AFIKOMEN", afikomen!!)
        super.onSaveInstanceState(outState)
    }

    /**
     * stops and unloads music
     */
    fun dieDayenu() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.reset()
                it.release()
                mediaPlayer = null
            }
        }
    }

    /** if program is paused, stop music */

    override fun onStop() {
        super.onStop()
        dieDayenu()
    }

    /**
     * click listener for box, checks to see if it matches afikomen, if so, plays song, show matzah
     * if not, hides box
     */
    inner class SearchListener(private val id: Int) : View.OnClickListener {
        override fun onClick(v: View?) {
            // Log.i("SearchListener","Id is " + id)
            if (startTime == 0L) {
                startTime = System.currentTimeMillis()
            }
            if (id == afikomen) {
                txtHeader.setText(R.string.afikomen_found)
                gameGrid.visibility = View.GONE
                imgAfikomen.visibility = View.VISIBLE
                resetButton.visibility = View.VISIBLE
                mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.dayenu)
                mediaPlayer!!.start()
                dancingImage()
                showTime((System.currentTimeMillis() - startTime) / 1000.0)

            } else {
                fadeBox(v!!)
                //v?.visibility = View.INVISIBLE
            }

        }

        /** Shows the time from the first box clicked to finding the afikomen
         * @param playTime {Double| time taken
         */
        private fun showTime(playTime: Double) {

            val snackbar = Snackbar.make(
                findViewById(R.id.mainLayout),
                getString(R.string.gameTime, playTime),
                Snackbar.LENGTH_LONG
            )
            val mainTextView: TextView =
                snackbar.view.findViewById(com.google.android.material.R.id.snackbar_text)
            // mainTextView.typeface = Typeface.create("casual",Typeface.BOLD)
            snackbar.view.setBackgroundColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.snackBack
                )
            )
            mainTextView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.snackText))
            mainTextView.textSize = 16.0F
            snackbar.show()
        }

        /**
         * Animates the Afikomen by scaling image, and spinning from -45 to 45 degrees
         */
        private fun dancingImage() {
            val animeTurn = ObjectAnimator.ofFloat(imgAfikomen, "rotation", -45f, 45f)
            animeTurn.duration = 800
            val scaleXAnimation =
                ObjectAnimator.ofFloat(imgAfikomen, "scaleX", 0.1f, 1.0f)
            scaleXAnimation.interpolator = AccelerateDecelerateInterpolator()
            scaleXAnimation.duration = 800
            val scaleYAnimation =
                ObjectAnimator.ofFloat(imgAfikomen, "scaleY", 0.01f, 1.0f)
            scaleYAnimation.interpolator = AccelerateDecelerateInterpolator()
            scaleYAnimation.duration = 800
            val animatorSet = AnimatorSet()
            animatorSet.play(animeTurn).with(scaleXAnimation).with(scaleYAnimation)
            animatorSet.start()
        }

        /**
         * Fades out box when clicked
         * @param v {View} box to fade
         */
        private fun fadeBox(v: View) {
            val animateFade = ObjectAnimator.ofFloat(v, "alpha", 0F)
            animateFade.duration = 300
            animateFade.start()
        }
    }

    /**
     * Reset the game, stops the music, restores all hagaddahs and hides the afikomen
     */
    inner class ResetListener : View.OnClickListener {
        override fun onClick(v: View?) {
            //reset last afikomen image back to Hagadah
            dieDayenu()
            startTime = 0L
            txtHeader.setText(R.string.find_the_afikomen)
            imgAfikomen.visibility = View.GONE
            gameGrid.visibility = View.VISIBLE
            resetButton.visibility = View.GONE
            afikomen = Random.nextInt(0, 24)
            boxes.forEach {
                it.alpha = 1F
                // it.visibility = View.VISIBLE
            }
        }
    }
}
