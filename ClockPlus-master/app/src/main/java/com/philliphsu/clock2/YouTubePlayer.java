package com.philliphsu.clock2;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.philliphsu.clock2.ringtone.playback.AlarmRingtoneService;
import com.squareup.okhttp.Request;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;

public class YouTubePlayer extends YouTubeBaseActivity {
    private FirebaseFunctions mFunctions;
    YouTubePlayerView youtubePlayerView;
    ImageButton likeBtn;
    ImageButton unlikeBtn;
    String mUrl;
    String mSongId;
    Date mStartDate;
    com.google.android.youtube.player.YouTubePlayer.OnInitializedListener onInitializeListener;
    private com.google.android.youtube.player.YouTubePlayer mYoutubePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_tube_player);
        likeBtn = (ImageButton)findViewById(R.id.imageButtonLike);
        unlikeBtn = (ImageButton)findViewById(R.id.imageButtonUnlike);
        youtubePlayerView = (YouTubePlayerView)findViewById(R.id.youtube_Player_view);

        mFunctions = FirebaseFunctions.getInstance();


        onInitializeListener=new com.google.android.youtube.player.YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(com.google.android.youtube.player.YouTubePlayer.Provider provider, com.google.android.youtube.player.YouTubePlayer youTubePlayer, boolean b) {
                mYoutubePlayer = youTubePlayer;

                mFunctions.getHttpsCallable("getSongUrl")// from dataBase/server
                        .call()
                        .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                            @Override
                            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                HashMap<String, String> map = (HashMap<String, String>) task.getResult().getData();
                                mUrl= map.get("url");
                                mSongId = map.get("songId");
                                Log.d(TAG, "onCompleteonLikeClick:"+ mUrl);


                                mYoutubePlayer.loadVideo(mUrl,1);


                                 mStartDate = new Date();

                            }
                        });
            }

            @Override
            public void onInitializationFailure(com.google.android.youtube.player.YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };

        String key = getString(R.string.youtube_key);
        youtubePlayerView.initialize(key,onInitializeListener);

        /*
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                String key = getString(R.string.youtube_key);
                youtubePlayerView.initialize(key,onInitializeListener);
            }
        }, 500);*/
    }


    public void onLikeClick(View view){
        Date endDate = new Date();
        int numSeconds = (int)((endDate.getTime() - mStartDate.getTime()) / 1000);

        stopAndFinish();
        String secondsPlayed = Integer.toString(numSeconds);
        String like ="1";
        String played ="1";
        Map<String, String> data = new HashMap<>();
        data.put("sec", secondsPlayed);
        data.put("isPlayed", played);
        data.put("songId", mSongId);
        data.put("isLiked",like);
        mFunctions.getHttpsCallable("updateSongScore")
                .call(data);
        mFunctions.getHttpsCallable("updateUserSongHistory")
                .call(data);
    }

    public void onUnlikeClick(View view){
        Date endDate = new Date();
        int numSeconds = (int)((endDate.getTime() - mStartDate.getTime()) / 1000);

        stopAndFinish();
        String secondsPlayed = Integer.toString(numSeconds);

        String like ="0";
        String played ="1";
        Map<String, String> data = new HashMap<>();
        data.put("sec", secondsPlayed);
        data.put("isPlayed", played);
        data.put("songId", mSongId);
        data.put("isLiked",like);

        mFunctions.getHttpsCallable("updateUserSongHistory")
                .call(data);
    }

    protected final void stopAndFinish() {
        stopService(new Intent(this, AlarmRingtoneService.class));
        finish();
        Toast.makeText(getApplicationContext(),"Thank you, have a nice day!",
                Toast.LENGTH_SHORT).show();
    }
}
