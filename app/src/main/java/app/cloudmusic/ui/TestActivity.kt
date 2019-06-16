package app.cloudmusic.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import app.cloudmusic.R
import app.cloudmusic.widget.PlayPauseView

class TestActivity : AppCompatActivity() {
    internal lateinit var playPauseView: PlayPauseView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        playPauseView = findViewById<View>(R.id.play) as PlayPauseView
        playPauseView.setOnClickListener {
            //playPauseView.playOrPause();
        }
    }
}
