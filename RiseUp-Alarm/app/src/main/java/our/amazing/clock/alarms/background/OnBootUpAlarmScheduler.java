/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package our.amazing.clock.alarms.background;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import our.amazing.clock.R;
import our.amazing.clock.alarms.Alarm;
import our.amazing.clock.alarms.data.AlarmCursor;
import our.amazing.clock.alarms.data.AlarmsTableManager;
import our.amazing.clock.alarms.misc.AlarmController;
import our.amazing.clock.ringtone.playback.AlarmRingtoneService;
import our.amazing.clock.util.TimeFormatUtils;

import static android.view.accessibility.AccessibilityNodeInfo.ACTION_DISMISS;
import static our.amazing.clock.util.ParcelableUtil.getRingingObject;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */

//job intent service
public class OnBootUpAlarmScheduler extends JobService {
    private static final String TAG = "OnBootUpAlarmScheduler";
    public static final int JOB_ID = 1000;
    private static final int ONE_MIN = 60 * 1000;


    public static void schedule(Context context) {
        ComponentName component = new ComponentName(context, OnBootUpAlarmScheduler.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component)
                // schedule it to run any time between 1 - 5 minutes
                .setMinimumLatency(0)
                .setOverrideDeadline(0);

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        doMyWork();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // whether or not you would like JobScheduler to automatically retry your failed job.
        return false;
    }

    private void doMyWork() {
        AlarmController controller = new AlarmController(this, null);
        // IntentService works in a background thread, so this won't hold us up.
        AlarmCursor cursor = new AlarmsTableManager(this).queryEnabledAlarms();
        while (cursor.moveToNext()) {
            Alarm alarm = cursor.getItem();
            if (!alarm.isEnabled()) {
                throw new IllegalStateException(
                        "queryEnabledAlarms() returned alarm(s) that aren't enabled");
            }
            Log.i(TAG, "onHandleIntent: >> controller.scheduleAlarm " + alarm.hour());
            controller.scheduleAlarm(alarm, false);
        }
        cursor.close();

        Log.i("OnBootUpAlarmScheduler", "Completed service @ " + SystemClock.elapsedRealtime());
    }



/*
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, OnBootUpAlarmScheduler.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        AlarmController controller = new AlarmController(this, null);
        // IntentService works in a background thread, so this won't hold us up.
        AlarmCursor cursor = new AlarmsTableManager(this).queryEnabledAlarms();
        while (cursor.moveToNext()) {
            Alarm alarm = cursor.getItem();
            if (!alarm.isEnabled()) {
                throw new IllegalStateException(
                        "queryEnabledAlarms() returned alarm(s) that aren't enabled");
            }
            Log.i(TAG, "onHandleIntent: >> controller.scheduleAlarm " + alarm.hour());
            controller.scheduleAlarm(alarm, false);
        }
        cursor.close();

        Log.i("OnBootUpAlarmScheduler", "Completed service @ " + SystemClock.elapsedRealtime());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //toast("All work complete");
    }

//    final Handler mHandler = new Handler();
//
//    // Helper for showing tests
//    void toast(final CharSequence text) {
//        mHandler.post(new Runnable() {
//            @Override public void run() {
//                Toast.makeText(OnBootUpAlarmScheduler.this, text, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

*/
}
