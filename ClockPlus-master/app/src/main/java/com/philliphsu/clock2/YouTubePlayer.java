package com.philliphsu.clock2;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.view.ViewGroup;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.alarms.misc.AlarmController;
import com.philliphsu.clock2.alarms.misc.AlarmPreferences;
import com.philliphsu.clock2.ringtone.AlarmActivity;
import com.philliphsu.clock2.ringtone.playback.AlarmRingtoneService;
import com.philliphsu.clock2.ringtone.playback.RingtoneService;
import com.philliphsu.clock2.util.ParcelableUtil;
import com.philliphsu.clock2.util.TimeFormatUtils;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.philliphsu.clock2.ringtone.RingtoneActivity;

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
import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.alarms.misc.AlarmController;
import com.philliphsu.clock2.ringtone.playback.AlarmRingtoneService;
import com.squareup.okhttp.Request;
import com.philliphsu.clock2.alarms.ui.BaseAlarmViewHolder;
import com.philliphsu.clock2.ringtone.playback.RingtoneLoop;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

import butterknife.Bind;
import butterknife.OnCheckedChanged;

import static android.support.constraint.Constraints.TAG;

public class YouTubePlayer extends YouTubeBaseActivity {
    public static final String EXTRA_RINGING_OBJECT = "com.philliphsu.clock2.ringtone.extra.RINGING_OBJECT";

    private AlarmController mAlarmController;
    private AudioManager mAudioManager;
    private FirebaseFunctions mFunctions;
    YouTubePlayerView youtubePlayerView;
    ImageButton likeBtn;
    ImageButton unlikeBtn;
    Button dismiss;
    Button snooze;
    TextView listen;
    TextView songName;
    TextView rateSong;

    String mSongName;
    String mUrl;
    String mSongId;
    Date mStartDate;
    com.google.android.youtube.player.YouTubePlayer.OnInitializedListener onInitializeListener;
    private com.google.android.youtube.player.YouTubePlayer mYoutubePlayer;
    private int mOrigionalVolume;
    private boolean isSnooze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setVolMax();

        mAlarmController = new AlarmController(this, null);
        setContentView(R.layout.activity_you_tube_player);
        rateSong = (TextView) findViewById(R.id.textViewRate);
        songName = (TextView) findViewById(R.id.textViewSongName);
        listen = (TextView) findViewById(R.id.textViewListen);
        snooze = (Button)findViewById(R.id.buttonSnooze);
        dismiss = (Button) findViewById(R.id.buttonDismiss);
        likeBtn = (ImageButton) findViewById(R.id.imageButtonLike);
        unlikeBtn = (ImageButton) findViewById(R.id.imageButtonUnlike);
        youtubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_Player_view);

        mFunctions = FirebaseFunctions.getInstance();

        onInitializeListener = new com.google.android.youtube.player.YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(com.google.android.youtube.player.YouTubePlayer.Provider provider, com.google.android.youtube.player.YouTubePlayer youTubePlayer, boolean b) {
                mYoutubePlayer = youTubePlayer;
                mYoutubePlayer.setPlaybackEventListener(new com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener() {
                    @Override
                    public void onPlaying() {

                    }

                    @Override
                    public void onPaused() {
                        Log.d(TAG, "onPaused:");

                    }

                    @Override
                    public void onStopped() {
                        Log.d(TAG, "onStopped:");
                        mYoutubePlayer.play();
                    }

                    @Override
                    public void onBuffering(boolean b) {

                    }

                    @Override
                    public void onSeekTo(int i) {

                    }
                });

                byte[] bytes = getIntent().getByteArrayExtra(EXTRA_RINGING_OBJECT);
                if (bytes == null) {
                    throw new IllegalStateException("Cannot start RingtoneActivity without a ringing object");
                }

               Alarm a =(Alarm) ParcelableUtil.unmarshall(bytes, Alarm.CREATOR);


                //if (!a.isSnoozed()){ // if not snoozing get song url from server
                  if(!ParcelableUtil.isSnooze()){
                    mFunctions.getHttpsCallable("getSongUrl")// from dataBase/server
                            .call()
                            .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                                @Override
                                public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                    HashMap<String, String> map = (HashMap<String, String>) task.getResult().getData();
                                    mUrl = map.get("url");
                                    mSongName = map.get("title");
                                    mSongId = map.get("songId");
                                    songName.setText(mSongName);
                                    Log.d(TAG, "onCompleteonLikeClick:" + mSongName);
                                    listen.setVisibility(View.VISIBLE);
                                    songName.setVisibility(View.VISIBLE);

                                    mYoutubePlayer.loadVideo(mUrl, 1);
                                    mYoutubePlayer.setPlayerStyle(com.google.android.youtube.player.YouTubePlayer.PlayerStyle.CHROMELESS);

                                    mStartDate = new Date();
                                }
                            });
                }else{
                    mSongName = ParcelableUtil.getSongName();
                    mUrl=ParcelableUtil.getSongUrl();
                    mSongId = ParcelableUtil.getSongId();
                    songName.setText(mSongName);
                    listen.setVisibility(View.VISIBLE);
                    songName.setVisibility(View.VISIBLE);
                    mYoutubePlayer.loadVideo(mUrl, 1);
                    mYoutubePlayer.setPlayerStyle(com.google.android.youtube.player.YouTubePlayer.PlayerStyle.CHROMELESS);
                    mStartDate = new Date();
                }

            }

            @Override
            public void onInitializationFailure(com.google.android.youtube.player.YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }

        };


        String key = getString(R.string.youtube_key);
        youtubePlayerView.initialize(key, onInitializeListener);

    }

    private void setVolMax() {
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mOrigionalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

    }

    private void setVolOriginal(){
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOrigionalVolume, 0);
    }

    public void onSnoozeClick(View view){
        byte[] bytes = getIntent().getByteArrayExtra(EXTRA_RINGING_OBJECT);
        if (bytes == null) {
            throw new IllegalStateException("Cannot start RingtoneActivity without a ringing object");
        }
        Alarm alarm = (Alarm) ParcelableUtil.unmarshall(bytes, Alarm.CREATOR);

        mAlarmController.snoozeAlarm(alarm);
//        int minutesToSnooze = AlarmPreferences.snoozeDuration(getApplicationContext());
//        alarm.snooze(minutesToSnooze);
//        getIntent().putExtra(AlarmActivity.EXTRA_RINGING_OBJECT, ParcelableUtil.marshall(alarm));
        ParcelableUtil.saveForSnooze(mSongName,mUrl,mSongId);
        Intent i = new Intent(this, AlarmRingtoneService.class);
        stopService(i);
        finish();
    }

    public void onDismissClick(View view) {
        ParcelableUtil.reset();
        //mAlarmController.cancelAlarm(getRingingObject(), false, true);

        mYoutubePlayer.pause();
        dismiss.setVisibility(View.INVISIBLE);
        snooze.setVisibility(View.INVISIBLE);
        likeBtn.setVisibility(View.VISIBLE);
        unlikeBtn.setVisibility(View.VISIBLE);
        rateSong.setVisibility(View.VISIBLE);
        stopService(new Intent(this, AlarmRingtoneService.class));

        Date endDate = new Date();
        int numSeconds = (int) ((endDate.getTime() - mStartDate.getTime()) / 1000);

        //stopAndFinish();
        String secondsPlayed = Integer.toString(numSeconds);

        Map<String, String> data = new HashMap<String, String>();
        data.put("sec", secondsPlayed);
        data.put("songId", mSongId);

        mFunctions.getHttpsCallable("updateUserSongHistory")
                .call(data);
    }

    public void onLikeClick(View view) {
        stopAndFinish();
        String like = "1";
        Map<String, String> data = new HashMap<String, String>();
        data.put("songId", mSongId);
        data.put("isLiked", like);
        mFunctions.getHttpsCallable("updateSongScore")
                .call(data);

    }

    public void onUnlikeClick(View view) {
        stopAndFinish();
        String like = "0";
        Map<String, String> data = new HashMap<String, String>();
        data.put("songId", mSongId);
        data.put("isLiked", like);
        mFunctions.getHttpsCallable("updateSongScore")
                .call(data);

    }

    protected final void stopAndFinish() {

        Intent i = new Intent(this, AlarmRingtoneService.class);
        stopService(i);
        finish();


         byte[] bytes = getIntent().getByteArrayExtra(EXTRA_RINGING_OBJECT);
        if (bytes == null) {
            throw new IllegalStateException("Cannot start RingtoneActivity without a ringing object");
        }

        setVolOriginal();
        mAlarmController.cancelAlarm((Alarm) ParcelableUtil.unmarshall(bytes, Alarm.CREATOR), false, true);
        Toast.makeText(getApplicationContext(), "Thank you, have a nice day!",
                Toast.LENGTH_SHORT).show();

    }
}

