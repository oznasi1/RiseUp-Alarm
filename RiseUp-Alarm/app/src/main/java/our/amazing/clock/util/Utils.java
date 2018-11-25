package our.amazing.clock.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Timer;

import our.amazing.clock.alarms.Alarm;

public final class Utils {
    static String mSongName;
    static String mUrl;
    static String mSongId;
    static boolean isSnooze = false;
    static int mNumSec;
    static Boolean mIsFinish = false;
    static Timer t = new Timer();
    static Boolean mIsPlaying = false;
    static Alarm mRingingObject ;

    public static void saveForSnooze(String songName, String songUrl, String songId, int numSec) {
        isSnooze = true;
        mSongName = songName;
        mUrl = songUrl;
        mSongId = songId;
        mNumSec = numSec;
    }

    public static boolean isSnooze() {
        return isSnooze;
    }

    public static String getSongName() {
        return mSongName;
    }


    public static String getSongId() {
        return mSongId;
    }

    public static String getSongUrl() {
        return mUrl;
    }

    public static int getNumSec() {
        return mNumSec;
    }

    public static void reset() {
        mUrl = "";
        mSongName = "";
        mSongId = "";
        isSnooze = false;
        mNumSec = 0;
    }

    public static boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static void setFinishOn() {
        mIsFinish = true;
    }

    public static Boolean isFinished() {
        return mIsFinish;
    }

    public static void setTimer(Timer x) {
        t = x;
    }

    public static Timer getTimer() {
        return t;
    }


    public static void setFinishOff() {
        mIsFinish = false;
    }

    public static void setOffOnPlaying() {
        mIsPlaying = false;
    }

    public static void setOnPlaying() {
        mIsPlaying = true;
    }

    public static Boolean isPlaying() {
        return mIsPlaying;
    }

    public static void saveRingingObject(Alarm ringingObject){
        mRingingObject = ringingObject;
    }

    public static Alarm getRingingObject(){
        return mRingingObject;
    }
}



/*

public final class ParcelableUtil {
    static String mSongName;
    static String mUrl;
    static String mSongId;
    static boolean isSnooze = false;
    static int mNumSec;
    static Boolean mIsFinish = false;
    static Timer t = new Timer();
    static Boolean mIsPlaying = false;
    static Alarm mRingingObject ;

    public static byte[] marshall(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // This is extremely important!
        return parcel;
    }

    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }

    public static void saveForSnooze(String songName, String songUrl, String songId, int numSec) {
        isSnooze = true;
        mSongName = songName;
        mUrl = songUrl;
        mSongId = songId;
        mNumSec = numSec;
    }

    public static boolean isSnooze() {
        return isSnooze;
    }

    public static String getSongName() {
        return mSongName;
    }


    public static String getSongId() {
        return mSongId;
    }

    public static String getSongUrl() {
        return mUrl;
    }

    public static int getNumSec() {
        return mNumSec;
    }

    public static void reset() {
        mUrl = "";
        mSongName = "";
        mSongId = "";
        isSnooze = false;
        mNumSec = 0;
    }

    public static boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static void setFinishOn() {
        mIsFinish = true;
    }

    public static Boolean isFinished() {
        return mIsFinish;
    }

    public static void setTimer(Timer x) {
        t = x;
    }

    public static Timer getTimer() {
        return t;
    }


    public static void setFinishOff() {
        mIsFinish = false;
    }

    public static void setOffOnPlaying() {
        mIsPlaying = false;
    }

    public static void setOnPlaying() {
        mIsPlaying = true;
    }

    public static Boolean isPlaying() {
        return mIsPlaying;
    }

    public static Alarm getAlarm(Intent i){
        byte[] bytes = i.getByteArrayExtra(EXTRA_RINGING_OBJECT);
        if (bytes == null) {
            throw new IllegalStateException("Cannot start RingtoneActivity without a ringing object");
        }
        Alarm alarm = (Alarm) ParcelableUtil.unmarshall(bytes, Alarm.CREATOR);

        return alarm;
    }

    public static void saveRingingObject(Alarm ringingObject){
        mRingingObject = ringingObject;
    }

    public static Alarm getRingingObject(){
        return mRingingObject;
    }
}



*/
