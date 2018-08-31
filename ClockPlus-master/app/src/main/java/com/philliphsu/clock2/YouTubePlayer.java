package com.philliphsu.clock2;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayerView;
import com.philliphsu.clock2.ringtone.playback.AlarmRingtoneService;

public class YouTubePlayer extends YouTubeBaseActivity {
    YouTubePlayerView youtubePlayerView;
    ImageButton likeBtn;
    ImageButton unlikeBtn;
    com.google.android.youtube.player.YouTubePlayer.OnInitializedListener onInitializeListener;

//    android:layout_alignParentTop="true"
//    android:layout_centerHorizonatal="true"
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_tube_player);
        likeBtn = (ImageButton)findViewById(R.id.imageButtonLike);
        unlikeBtn = (ImageButton)findViewById(R.id.imageButtonUnlike);
        youtubePlayerView = (YouTubePlayerView)findViewById(R.id.youtube_Player_view);


        onInitializeListener=new com.google.android.youtube.player.YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(com.google.android.youtube.player.YouTubePlayer.Provider provider, com.google.android.youtube.player.YouTubePlayer youTubePlayer, boolean b) {
                //getSongUrl(); from dataBase/server
                youTubePlayer.loadVideo("dvgZkm1xWPE");
            }

            @Override
            public void onInitializationFailure(com.google.android.youtube.player.YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                youtubePlayerView.initialize(PlayerConfig.API_KEY,onInitializeListener);
            }
        }, 3000);
    }


    public void onLikeClick(View view){
        stopAndFinish();
        //connection to server / database
    }

    public void onUnlikeClick(View view){
        stopAndFinish();
        //connection to server / database
        Toast.makeText(getApplicationContext(),"Thank you, have a nice day!",
                Toast.LENGTH_SHORT).show();
    }

    protected final void stopAndFinish() {
        stopService(new Intent(this, AlarmRingtoneService.class));
        finish();
        Toast.makeText(getApplicationContext(),"Thank you, have a nice day!",
                Toast.LENGTH_SHORT).show();
    }
}
