package our.amazing.clock;

import android.app.NotificationManager;
import android.content.Intent;
import android.media.AudioManager;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;

import our.amazing.clock.R;
import our.amazing.clock.alarms.Alarm;
import our.amazing.clock.alarms.misc.AlarmController;
import our.amazing.clock.ringtone.AlarmActivity;
import our.amazing.clock.ringtone.playback.AlarmRingtoneService;
import our.amazing.clock.util.ParcelableUtil;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import our.amazing.clock.ringtone.playback.RingtoneLoop;

import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

import static android.support.constraint.Constraints.TAG;

public class YouTubePlayer extends YouTubeBaseActivity implements com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener {
    public static final String EXTRA_RINGING_OBJECT = "our.amazing.clock.ringtone.extra.RINGING_OBJECT";
    private AlarmController mAlarmController;
    private AudioManager mAudioManager;
    private FirebaseFunctions mFunctions;
    private RingtoneLoop mRingtoneLoop;
    private Vibrator mVibrator;
    private NotificationManager mNotificationManager;
    ImageView noInternetConnection;
    YouTubePlayerView youtubePlayerView;
    ImageButton likeBtn;
    ImageButton unlikeBtn;
    ImageView dismiss;
    ImageView snooze;
    TextView listen;
    TextView songName;
    TextView rateSong;
    String mSongName;
    String mUrl;
    String mSongId;
    Date mStartDate;
    Boolean isFinish= false;
    int numOfSecPlayed=0;
    private int count=0;
    private Timer t;
    private Handler h;
    com.google.android.youtube.player.YouTubePlayer.OnInitializedListener onInitializeListener;
    private com.google.android.youtube.player.YouTubePlayer mYoutubePlayer;
    private int mOrigionalMediaVolume;
    private boolean isErrorLoading;

    Runnable runnable;
    Handler handler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!ParcelableUtil.isPlaying()) {


            ParcelableUtil.setOnPlaying();

            adjustColor();
            adjustVibrate();

            isFinish = ParcelableUtil.isFinished();

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
                            if(mRingtoneLoop==null){
                                mRingtoneLoop = new RingtoneLoop(getApplicationContext(), Settings.System.DEFAULT_ALARM_ALERT_URI);
                                mRingtoneLoop.play();
                            }
                            addClickLisenersSnoozeAndDismiss();
                        }
                    });

                    mYoutubePlayer.setPlaybackEventListener(new com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener() {
                        @Override
                        public void onPlaying() {
                            addClickLisenersSnoozeAndDismiss();
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
                        ParcelableUtil.setFinishOff();
                        mSongName = ParcelableUtil.getSongName();
                        mUrl = ParcelableUtil.getSongUrl();
                        mSongId = ParcelableUtil.getSongId();
                        numOfSecPlayed = ParcelableUtil.getNumSec();
                        songName.setText(mSongName);
                        listen.setVisibility(View.VISIBLE);
                        songName.setVisibility(View.VISIBLE);
                        if (mUrl == null) {
                            getSongUrlFromServer();
                        } else {
                            mYoutubePlayer.loadVideo(mUrl, 1);
                            mYoutubePlayer.setPlayerStyle(com.google.android.youtube.player.YouTubePlayer.PlayerStyle.CHROMELESS);

                            mStartDate = new Date();
                        }
//                    mYoutubePlayer.loadVideo(mUrl, 1);
//                    mYoutubePlayer.setPlayerStyle(com.google.android.youtube.player.YouTubePlayer.PlayerStyle.CHROMELESS);
//
//                    mStartDate = new Date();
                    }
                }

                @Override
                public void onInitializationFailure(com.google.android.youtube.player.YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                    Log.e(TAG, "onInitializationFailure: ");


                }

            };


            String key = getString(R.string.youtube_key);
            youtubePlayerView.initialize(key, onInitializeListener);
        }else{
            mAlarmController = new AlarmController(this, null);

            finish();

            Alarm alarm = getAlarm();

            //setVolOriginal();
            mAlarmController.cancelAlarm(alarm, false, true);
        }
    }

    private void addClickLisenersSnoozeAndDismiss(){
        dismiss.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismissClick();
            }
        });

        snooze.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                snoozeClick();
            }
        });
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
    public void onBackPressed() {
        // Capture the back press and return. We want to limit the user's options for leaving
        // this activity as much as possible.
    }

    @Override
    public void onError(com.google.android.youtube.player.YouTubePlayer.ErrorReason errorReason) {
        Log.e(TAG, "onError: ");
        isErrorLoading = true;
        updateUiAccordingToInternetConnecion();
        mRingtoneLoop = new RingtoneLoop(getApplicationContext(), Settings.System.DEFAULT_ALARM_ALERT_URI);
        mRingtoneLoop.play();
        addClickLisenersSnoozeAndDismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        Date endDate = new Date();
        int numSeconds = (int) ((endDate.getTime() - mStartDate.getTime()) / 1000); // seconds played until now
        numOfSecPlayed += numSeconds;

        if (ParcelableUtil.isSnooze()) {
            ParcelableUtil.setFinishOff();
        }

        //isFinish = ParcelableUtil.isFinished();
        if(mVibrator!=null){
            mVibrator.cancel();
        }

        if(count!=0&&!isFinish){

            runnable = new Runnable() {
                public void run() {
                    Intent i = new Intent(getApplicationContext(),YouTubePlayer.class)
                            .putExtra(AlarmActivity.EXTRA_RINGING_OBJECT, ParcelableUtil.marshall(getAlarm()));
                    ParcelableUtil.saveForSnooze(mSongName,mUrl,mSongId,numOfSecPlayed);
                    if(isErrorLoading){
                        mRingtoneLoop.stop();
                    }
                    if(mVibrator!=null){
                        mVibrator.cancel();
                    }

                    finish();
                    ParcelableUtil.setOffOnPlaying();

                    startActivity(i);
                }
            };

            handler = new android.os.Handler();
            handler.postDelayed(runnable, 30000);

        }

        count++;
        ParcelableUtil.setFinishOff();

    }

    @Override
    public void onResume(){
        super.onResume();
        if(handler!=null){
            handler.removeCallbacks(runnable);
        }
        adjustVibrate();
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

    private void setVolOriginal(){
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOrigionalMediaVolume, 0);
    }

    public void snoozeClick(){
        ParcelableUtil.setOffOnPlaying();

        ParcelableUtil.setFinishOn();
        isFinish = true;
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

    public void dismissClick() {
        ParcelableUtil.setFinishOn();
        isFinish=true;
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
        isFinish = ParcelableUtil.isFinished();
        updateScore(true);
    }

    public void onUnlikeClick(View view) {
        isFinish = ParcelableUtil.isFinished();
        updateScore(false);
    }

    protected final void stopAndFinish() {
        Intent ix = new Intent(this, AlarmRingtoneService.class);
        stopService(ix);
        finish();



        Alarm alarm = getAlarm();

        setVolOriginal();
        mAlarmController.cancelAlarm(alarm, false, true);
        Toast.makeText(getApplicationContext(), "Thank you, have a nice day!",
                Toast.LENGTH_SHORT).show();

        ParcelableUtil.setOffOnPlaying();

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }
}

