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
import our.amazing.clock.ringtone.RingtoneActivity;
import our.amazing.clock.ringtone.playback.AlarmRingtoneService;
import our.amazing.clock.util.ParcelableUtil;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerInitListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.ui.PlayerUIController;
import com.pierfrancescosoffritti.androidyoutubeplayer.utils.YouTubePlayerTracker;

import our.amazing.clock.ringtone.playback.RingtoneLoop;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

import static android.support.constraint.Constraints.TAG;
import static our.amazing.clock.util.DelayedSnackbarHandler.show;
import static our.amazing.clock.util.ParcelableUtil.getRingingObject;

//        setVolumeControlStream(AudioManager.STREAM_ALARM);
public class playAlarmActivity extends YouTubeBaseActivity {
    private static final String TAG = "playAlarmActivity";
    public static final String EXTRA_RINGING_OBJECT = "our.amazing.clock.ringtone.extra.RINGING_OBJECT";
    public AlarmController mAlarmController;
    private AudioManager mAudioManager;
    private FirebaseFunctions mFunctions;
    private FirebaseAnalytics mFirebaseAnalytics;
    private RingtoneLoop mRingtoneLoop;
    private Vibrator mVibrator;
    private ImageView noInternetConnection;
    //private com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView youtubePlayerView;
    private YouTubePlayerTracker tracker;
    private ImageButton likeBtn;
    private ImageButton unlikeBtn;
    private ImageView dismiss;
    private ImageView snooze;
    private TextView listen;
    private TextView songName;
    private TextView timeDisplay;
    private String mSongName;
    private String mUrl;
    private String mSongId;
    private Date mStartDate;
    private Boolean isFinish= false;
    private int numOfSecPlayed=0;
    private int count=0;
    private YouTubePlayerView youtubePlayerView;
    private com.google.android.youtube.player.YouTubePlayer.OnInitializedListener onInitializeListener;
    private com.google.android.youtube.player.YouTubePlayer mYoutubePlayer;
    //private com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer mYoutubePlayer;
    private int mOrigionalMediaVolume;
    private boolean isErrorLoading;
    private Runnable runnable;
    private Handler handler;
    private Runnable runnableBuffer;
    private Handler handlerBuffer;
    private Runnable runnableStart;
    private Handler handlerStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ParcelableUtil.isPlaying()) {

            try {
                ParcelableUtil.setOnPlaying();
                adjustColor();
                isFinish = ParcelableUtil.isFinished();

                addFlagToActivity();

                mAlarmController = new AlarmController(this, null);
                mAlarmController.removeUpcomingAlarmNotification(getAlarm());

                mFunctions = FirebaseFunctions.getInstance();
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

                setContentView(R.layout.activity_play_alarm);

                bindViewsById();

                adjustments();

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
                                playDefaultRingtone(0);
                            }
                        });

                        mYoutubePlayer.setPlaybackEventListener(new com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener() {
                            @Override
                            public void onPlaying() {
                                //findViewById(R.id.loadingPanel).setVisibility(View.GONE);
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
                            restoreDataForSnooze();
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
                        }
                    }

                    @Override
                    public void onInitializationFailure(com.google.android.youtube.player.YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                        Log.e(TAG, "onInitializationFailure: ");
                        isErrorLoading=true;
                        playDefaultRingtone(0);
                    }

                };

                String key = getString(R.string.youtube_key);
                youtubePlayerView.initialize(key, onInitializeListener);
            } catch (Exception e) {

                try {
                    setContentView(R.layout.activity_play_alarm);
                    bindViewsById();
                    adjustments();
                    isErrorLoading = true; //try onCreate (5)
                    playDefaultRingtone(5);
                } catch (Exception el) {
                    mAlarmController = new AlarmController(this, null);
                    finish();
                    Alarm alarm = getAlarm();
                    //setVolOriginal();
                    mAlarmController.cancelAlarm(alarm, false, true);
                    Toast.makeText(getApplicationContext(), "an error occoured",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else { // the main lock is locked and we go back to main activity
            mAlarmController = new AlarmController(this, null);
            finish();
            Alarm alarm = getAlarm();
            //setVolOriginal();
            mAlarmController.cancelAlarm(alarm, false, true);
        }
    }

    private void restoreDataForSnooze() {
        mSongName = ParcelableUtil.getSongName();
        mUrl = ParcelableUtil.getSongUrl();
        mSongId = ParcelableUtil.getSongId();
        numOfSecPlayed = ParcelableUtil.getNumSec();
    }

    private void adjustments() {
        //adjustColor();
        adjustVibrate();
        adjustVolume();
        adjustLocale();
        adjustTimeDisplay();
    }

    private void adjustTimeDisplay(){
        DateFormat df = new SimpleDateFormat("HH:mm");
        Date dateobj = new Date();
        timeDisplay.setText(df.format(dateobj));
    }

    private void bindViewsById() {
        noInternetConnection = (ImageView) findViewById(R.id.imageViewNoInternet);
        timeDisplay = (TextView) findViewById(R.id.textViewTimeDisplay);
        songName = (TextView) findViewById(R.id.textViewSongName);
        listen = (TextView) findViewById(R.id.textViewListen);
        snooze = (ImageView) findViewById(R.id.imageViewSnooze);
        dismiss = (ImageView) findViewById(R.id.imageViewDismiss);
        likeBtn = (ImageButton) findViewById(R.id.imageButtonLike);
        unlikeBtn = (ImageButton) findViewById(R.id.imageButtonUnlike);
        youtubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player_view);
    }

    private void addFlagToActivity() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        getWindow().addFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
                    Intent i = new Intent(getApplicationContext(),playAlarmActivity.class)
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
            handler.postDelayed(runnable, 20000);

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
        if(mYoutubePlayer!=null){
            mYoutubePlayer.play();
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

    @Override
    public void onBackPressed(){

    }

    private void sendFirebaseLogs(int num){
        Bundle params = new Bundle();
        params.putString("number", Integer.toString(num));
        mFirebaseAnalytics.logEvent("error_number", params);
    }

    private void playDefaultRingtone(int num) {
        sendFirebaseLogs(num);
        //findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        updateUiAccordingToInternetConnecion();
        if (mRingtoneLoop ==null){
            mRingtoneLoop = new RingtoneLoop(getApplicationContext(), Settings.System.DEFAULT_ALARM_ALERT_URI);
            mRingtoneLoop.play();
        }
        addClickLisenersSnoozeAndDismiss();
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
            Log.e(TAG, "onCompleteUrlSong: we catched"+ e.toString());
            isErrorLoading = true; // catch in onCreate (2)
            playDefaultRingtone(2);
        }
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

    private void updateUiAccordingToInternetConnecion(){
        youtubePlayerView.setVisibility(View.INVISIBLE);
        noInternetConnection.setVisibility(View.VISIBLE);
        listen.setVisibility(View.INVISIBLE);
        songName.setVisibility(View.INVISIBLE);
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

        stopAlarmRingtoneService();
        finish();
    }

    private void adjustColor(){
        //final String themeDark = getString(R.string.theme_dark);
        final String themeBlack = getString(R.string.theme_black);
        String theme = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.key_theme), null);
        if(themeBlack.equals(theme)) {
            setTheme(R.style.AppTheme_Black);
        }
    }

    private void adjustLocale(){
        Locale current = getResources().getConfiguration().locale;
        if(current.toString().contains("IL")) {
            snooze.setImageResource(R.drawable.snoozesubtitleheb);
            dismiss.setImageResource(R.drawable.dismisssubtitleheb);
        }
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

            stopAlarmRingtoneService();

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

    private void stopAlarmRingtoneService() {
//        Intent i = new Intent(this, AlarmRingtoneService.class);
//        stopService(i);
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
    stopAlarmRingtoneService();
        finish();

        Alarm alarm = getAlarm();

        setVolOriginal();
        mAlarmController.cancelAlarm(alarm, false, true);
        if(!isErrorLoading){
            Toast.makeText(getApplicationContext(), R.string.have_nice_day,
                    Toast.LENGTH_SHORT).show();
        }

        ParcelableUtil.setOffOnPlaying();

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

}

