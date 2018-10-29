package com.philliphsu.clock2;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.support.v4.app.NotificationCompat;
import android.view.ViewGroup;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.alarms.Alarm;
import com.philliphsu.clock2.alarms.background.UpcomingAlarmReceiver;
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
import android.widget.ImageView;
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
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnCheckedChanged;

import static android.support.constraint.Constraints.TAG;

public class YouTubePlayer extends YouTubeBaseActivity implements com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener {
    public static final String EXTRA_RINGING_OBJECT = "com.philliphsu.clock2.ringtone.extra.RINGING_OBJECT";
    private AlarmController mAlarmController;
    private AudioManager mAudioManager;
    private FirebaseFunctions mFunctions;
    private RingtoneLoop mRingtoneLoop;
    private NotificationManager mNotificationManager ;
    private Vibrator mVibrator;

    ImageView noInternetConnection;
    YouTubePlayerView youtubePlayerView;
    ImageButton likeBtn;
    ImageButton unlikeBtn;
    ImageView dismiss;
    ImageView snooze;
    TextView listen;
    TextView songName;
    TextView rateSong;
    TextView mTextViewDismiss;
    TextView mTextViewSnooze;
    String mSongName;
    String mUrl;
    String mSongId;
    Date mStartDate;
    int numOfSecPlayed=0;
    com.google.android.youtube.player.YouTubePlayer.OnInitializedListener onInitializeListener;
    private com.google.android.youtube.player.YouTubePlayer mYoutubePlayer;
    private int mOrigionalMediaVolume;
    private boolean isErrorLoading;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustColor();
        adjustVibrate();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        adjustVolume();

        mAlarmController = new AlarmController(this, null);
        setContentView(R.layout.activity_you_tube_player);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //handleNotification();
        noInternetConnection = (ImageView) findViewById(R.id.imageViewNoInternet);
        rateSong = (TextView) findViewById(R.id.textViewRate);
        songName = (TextView) findViewById(R.id.textViewSongName);
        listen = (TextView) findViewById(R.id.textViewListen);
        snooze = (ImageView) findViewById(R.id.imageViewSnooze);
        dismiss = (ImageView) findViewById(R.id.imageViewDismiss);
        likeBtn = (ImageButton) findViewById(R.id.imageButtonLike);
        unlikeBtn = (ImageButton) findViewById(R.id.imageButtonUnlike);
        youtubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_Player_view);

        adjustLocale();

        mFunctions = FirebaseFunctions.getInstance();
        onInitializeListener = new com.google.android.youtube.player.YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(com.google.android.youtube.player.YouTubePlayer.Provider provider, com.google.android.youtube.player.YouTubePlayer youTubePlayer, boolean b) {
                mYoutubePlayer = youTubePlayer;

                mYoutubePlayer.setPlayerStateChangeListener(new com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener() {
                    @Override
                    public void onLoading() {

                    }

                    @Override
                    public void onLoaded(String s) {

                    }

                    @Override
                    public void onAdStarted() {

                    }

                    @Override
                    public void onVideoStarted() {

                    }

                    @Override
                    public void onVideoEnded() {
                        mYoutubePlayer.play();
                    }

                    @Override
                    public void onError(com.google.android.youtube.player.YouTubePlayer.ErrorReason errorReason) {
                        Log.e(TAG, "onError: ");
                        isErrorLoading = true;
                        updateUiAccordingToInternetConnecion();
                        mRingtoneLoop = new RingtoneLoop(getApplicationContext(), Settings.System.DEFAULT_ALARM_ALERT_URI);
                        mRingtoneLoop.play();
                    }
                });

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

                if (!ParcelableUtil.isSnooze()) {
                    getSongUrlFromServer();

                } else {
                    mSongName = ParcelableUtil.getSongName();
                    mUrl = ParcelableUtil.getSongUrl();
                    mSongId = ParcelableUtil.getSongId();
                    numOfSecPlayed = ParcelableUtil.getNumSec();
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
                Log.e(TAG, "onInitializationFailure: ");


            }

        };




        String key = getString(R.string.youtube_key);
        youtubePlayerView.initialize(key, onInitializeListener);
    }

    private void getSongUrlFromServer(){
        mFunctions.getHttpsCallable("getSongUrl")// from dataBase/server
                .call()
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        onCompleteUrlSong(task);
                    }
                });
    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onLoaded(String s) {

    }

    @Override
    public void onAdStarted() {

    }

    @Override
    public void onVideoStarted() {

    }

    @Override
    public void onVideoEnded() {

    }

    @Override
    public void onError(com.google.android.youtube.player.YouTubePlayer.ErrorReason errorReason) {
        Log.e(TAG, "onError: ");
        isErrorLoading = true;
        updateUiAccordingToInternetConnecion();
        mRingtoneLoop = new RingtoneLoop(getApplicationContext(), Settings.System.DEFAULT_ALARM_ALERT_URI);
        mRingtoneLoop.play();
    }

    @Override
    public void onPause(){
        super.onPause();
        Date endDate = new Date();
        int numSeconds = (int) ((endDate.getTime() - mStartDate.getTime()) / 1000); // seconds played until now
        numOfSecPlayed +=numSeconds;
    }

    @Override
    public void onResume(){
        super.onResume();
        mStartDate = new Date();
    }
    private void adjustVibrate(){
        if (getAlarm().vibrates()) {
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            mVibrator.vibrate(new long[] { // apply pattern
                    0, // millis to wait before turning vibrator on
                    500, // millis to keep vibrator on before turning off
                    500, // millis to wait before turning back on
                    500 // millis to keep on before turning off
            }, 2 /* start repeating at this index of the array, after one cycle */);
        }
    }

    private void onCompleteUrlSong( Task<HttpsCallableResult> task){
        try {
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
        catch (Exception e){
            //TODO: deal with map = null -> server error
            Log.e(TAG, "onCompleteUrlSong: we catched"+ e.toString());
            isErrorLoading = true;
            updateUiAccordingToInternetConnecion();
            mRingtoneLoop = new RingtoneLoop(getApplicationContext(), Settings.System.DEFAULT_ALARM_ALERT_URI);
            mRingtoneLoop.play();
        }
    }

    private void updateUiAccordingToInternetConnecion(){
            youtubePlayerView.setVisibility(View.INVISIBLE);
            noInternetConnection.setVisibility(View.VISIBLE);
            listen.setVisibility(View.INVISIBLE);
            songName.setVisibility(View.INVISIBLE);
    }

    private void adjustVolume() {
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mOrigionalMediaVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        int volAlarm = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if(volAlarm <= 3){
            volAlarm = 7;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volAlarm, 0);
    }

    private void handleNotification(){
        Intent intent = new Intent(getApplicationContext(), Blank.class);

        this.mNotificationManager.notify(12321, new Notification.Builder(this)
                .setContentTitle("").setContentText("")
                .setSmallIcon(R.drawable.ic_add_24dp)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setFullScreenIntent(PendingIntent.getService(getApplicationContext(),12321,intent,PendingIntent.FLAG_NO_CREATE), true)
                .setAutoCancel(true)
                .build());
    }
    private void setVolOriginal(){
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOrigionalMediaVolume, 0);
    }

    public void onSnoozeClick(View view){
        Alarm alarm = getAlarm();
        if(mVibrator!=null){
            mVibrator.cancel();
        }
        mAlarmController.snoozeAlarm(alarm);
        if(!isErrorLoading){
            Date endDate = new Date();
            int numSeconds = (int) ((endDate.getTime() - mStartDate.getTime()) / 1000);
            numOfSecPlayed +=numSeconds;
            ParcelableUtil.saveForSnooze(mSongName,mUrl,mSongId,numOfSecPlayed);
        }
        else{
            mRingtoneLoop.stop();
        }

        Intent i = new Intent(this, AlarmRingtoneService.class);
        stopService(i);
        finish();
    }

    private void adjustColor(){
        final String themeDark = getString(R.string.theme_dark);
        final String themeBlack = getString(R.string.theme_black);
        String theme = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.key_theme), null);
        if (themeDark.equals(theme)) {
            setTheme(R.style.AppTheme_Dark);
        } else if (themeBlack.equals(theme)) {
            setTheme(R.style.AppTheme_Black);
        }
    }

    private void adjustLocale(){
        Locale current = getResources().getConfiguration().locale;
        if(current.toString().contains("IL")) {
            listen.setText("אתה מאזין כעת ל:");
            snooze.setImageResource(R.drawable.snoozesubtitleheb);
            dismiss.setImageResource(R.drawable.dismisssubtitleheb);
            rateSong.setText("בבקשה דרג את השיר , על מנת שבפעם הבאה נתאים לך שירים שתאהב יותר");
        }
    }

    private Alarm getAlarm(){
        byte[] bytes = getIntent().getByteArrayExtra(EXTRA_RINGING_OBJECT);
        if (bytes == null) {
            throw new IllegalStateException("Cannot start RingtoneActivity without a ringing object");
        }
        Alarm alarm = (Alarm) ParcelableUtil.unmarshall(bytes, Alarm.CREATOR);

        return alarm;
    }

    public void onDismissClick(View view) {
        ParcelableUtil.reset();
        if(mVibrator!=null){
            mVibrator.cancel();
        }
        if(!isErrorLoading){
            mYoutubePlayer.pause();
            dismiss.setVisibility(View.INVISIBLE);
            snooze.setVisibility(View.INVISIBLE);
            likeBtn.setVisibility(View.VISIBLE);
            unlikeBtn.setVisibility(View.VISIBLE);
            rateSong.setVisibility(View.VISIBLE);

            stopService(new Intent(this, AlarmRingtoneService.class));

            Date endDate = new Date();
            int numSeconds = (int) ((endDate.getTime() - mStartDate.getTime()) / 1000);
            numOfSecPlayed +=numSeconds;
            String secondsPlayed = Integer.toString(numOfSecPlayed);
            Map<String, String> data = new HashMap<String, String>();
            data.put("sec", secondsPlayed);
            data.put("songId", mSongId);

            mFunctions.getHttpsCallable("updateUserSongHistory")
                    .call(data);
        }
        else{
            mRingtoneLoop.stop();
            stopAndFinish();
        }
    }

    private void updateScore(boolean isLike ){
        stopAndFinish();
        String isUserLiked = isLike ? "1" : "0";
        final Map<String, String> data = new HashMap<String, String>();
        data.put("songId", mSongId);
        data.put("isLiked", isUserLiked);
        mFunctions.getHttpsCallable("updateSongScore")
                .call(data);
    }

    public void onLikeClick(View view) {
        updateScore(true);
    }

    public void onUnlikeClick(View view) {
        updateScore(false);
    }

    protected final void stopAndFinish() {
        Intent i = new Intent(this, AlarmRingtoneService.class);
        stopService(i);
        finish();

        Alarm alarm = getAlarm();

        setVolOriginal();
        mAlarmController.cancelAlarm(alarm, false, true);
        Toast.makeText(getApplicationContext(), "Thank you, have a nice day!",
                Toast.LENGTH_SHORT).show();
    }
}
