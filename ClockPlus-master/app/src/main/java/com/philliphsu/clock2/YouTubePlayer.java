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

import java.io.BufferedReader;
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

        mFunctions = FirebaseFunctions.getInstance();


        onInitializeListener=new com.google.android.youtube.player.YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(com.google.android.youtube.player.YouTubePlayer.Provider provider, com.google.android.youtube.player.YouTubePlayer youTubePlayer, boolean b) {
                //getSongUrl();// from dataBase/server

               //addMessage("hello");

                youTubePlayer.loadVideo("OfZUDlv6jno");
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

//    private Task<String> addMessage(String text) {
//        // Create the arguments to the callable function.
//        Map<String, Object> data = new HashMap<>();
//        data.put("text", text);
//        data.put("push", true);
//
//        return mFunctions
//                .getHttpsCallable("helloWorld")
//                .call(data)
//                .continueWith(new Continuation<HttpsCallableResult, String>() {
//                    @Override
//                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
//                        // This continuation runs on either success or failure, but if the task
//                        // has failed then getResult() will throw an Exception which will be
//                        // propagated down.
//                        String result = (String) task.getResult().getData();
//                        Log.d(TAG, "then: " + result);
//                        return result;
//                    }
//                });
//    }
private Task<String> addMessage(String text) {
    // Create the arguments to the callable function.
    final Map<String, Object> data = new HashMap<>();
    data.put("text", text);
    data.put("push", true);

    return mFunctions
            .getHttpsCallable("addMessage")
            .call(data)
            .continueWith(new Continuation<HttpsCallableResult, String>() {
                @Override
                public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.

                    String result = (String) task.getResult().getData();
                    return result;
                }
            });
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
