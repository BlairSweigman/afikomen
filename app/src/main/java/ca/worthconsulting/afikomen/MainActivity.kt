/*
 * Copyright (c) 2020. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Blair Sweigman <blair.sweigman@gmail.com>
 */

package ca.worthconsulting.afikomen

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.View
import android.widget.Button
import androidx.gridlayout.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlin.random.Random
import com.google.android.gms.ads.MobileAds

class MainActivity : AppCompatActivity() {
    private val boxes = mutableListOf<ImageView>()
    private var afikomen: Int? = null
    private lateinit var resetButton: Button
    private lateinit var txtHeader : TextView
    private lateinit var gameGrid: GridLayout
    private lateinit var imgAfikomen: ImageView
    lateinit var mAdView : AdView
    private var mediaPlayer: MediaPlayer? = null
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
            if (id == afikomen) {
                txtHeader.setText(R.string.afikomen_found)
                gameGrid.visibility = View.GONE
                imgAfikomen.visibility = View.VISIBLE
                resetButton.visibility=View.VISIBLE
                mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.dayenu)
                mediaPlayer!!.start()


            } else
                v?.visibility = View.INVISIBLE

        }
    }

    /**
     * Reset the game, stops the music, restores all hagaddahs and hides the afikomen
     */
    inner class ResetListener : View.OnClickListener {
        override fun onClick(v: View?) {
            //reset last afikomen image back to Hagadah
            dieDayenu()
            txtHeader.setText(R.string.find_the_afikomen)
            imgAfikomen.visibility = View.GONE
            gameGrid.visibility = View.VISIBLE
            resetButton.visibility=View.GONE
            afikomen = Random.nextInt(0, 24)
            boxes.forEach {
                it.visibility = View.VISIBLE
            }
        }
    }
}