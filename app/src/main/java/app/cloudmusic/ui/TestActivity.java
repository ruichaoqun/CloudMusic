package app.cloudmusic.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import app.cloudmusic.R;
import app.cloudmusic.widget.PlayPauseView;

public class TestActivity extends AppCompatActivity {
    PlayPauseView playPauseView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        playPauseView = (PlayPauseView) findViewById(R.id.play);
        playPauseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playPauseView.playOrPause();
            }
        });
    }
}
