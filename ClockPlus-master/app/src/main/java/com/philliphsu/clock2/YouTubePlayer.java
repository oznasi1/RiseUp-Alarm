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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;

public class YouTubePlayer extends YouTubeBaseActivity {
    private FirebaseFunctions mFunctions;
    YouTubePlayerView youtubePlayerView;
    ImageButton likeBtn;
    ImageButton unlikeBtn;
    String mUrl;
    com.google.android.youtube.player.YouTubePlayer.OnInitializedListener onInitializeListener;
    private com.google.android.youtube.player.YouTubePlayer mYoutubePlayer;


//    android:layout_alignParentTop="true"
//    android:layout_centerHorizonatal="true"
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_tube_player);
        likeBtn = (ImageButton)findViewById(R.id.imageButtonLike);
        unlikeBtn = (ImageButton)findViewById(R.id.imageButtonUnlike);
        youtubePlayerView = (YouTubePlayerView)findViewById(R.id.youtube_Player_view);

        mFunctions = FirebaseFunctions.getInstance();

        /*
        mFunctions.getHttpsCallable("getSongUrl")// from dataBase/server
                .call()
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        Log.d(TAG, "onCompleteGetSong:"+task.getResult().getData());
                        mUrl = task.getResult().getData().toString().substring(5);
                        mUrl = mUrl.substring(0,mUrl.length()-1);
                        Log.d(TAG, "onCompleteGetSong: "+mUrl);
                    }
                });
                */

        mFunctions.getHttpsCallable("helloWorld")
                .call()
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        Log.d(TAG, "onCompleteHello:"+task.getResult().getData());

                    }
                });

        onInitializeListener=new com.google.android.youtube.player.YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(com.google.android.youtube.player.YouTubePlayer.Provider provider, com.google.android.youtube.player.YouTubePlayer youTubePlayer, boolean b) {
                Log.d(TAG, "onComplete2:"+ mUrl);
                //youTubePlayer.loadVideo(mUrl);
                mYoutubePlayer = youTubePlayer;

                mFunctions.getHttpsCallable("getSongUrl")// from dataBase/server
                        .call()
                        .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                            @Override
                            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                Log.d(TAG, "onCompleteGetSong:"+task.getResult().getData());
                                mUrl = task.getResult().getData().toString().substring(5);
                                mUrl = mUrl.substring(0,mUrl.length()-1);
                                Log.d(TAG, "onCompleteGetSong: "+mUrl);

                                mYoutubePlayer.loadVideo(mUrl);
                            }
                        });
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
    }

    protected final void stopAndFinish() {
        stopService(new Intent(this, AlarmRingtoneService.class));
        finish();
        Toast.makeText(getApplicationContext(),"Thank you, have a nice day!",
                Toast.LENGTH_SHORT).show();
    }
}
